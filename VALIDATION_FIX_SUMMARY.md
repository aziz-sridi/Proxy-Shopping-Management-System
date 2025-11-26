## Summary of Validation Fix Applied

### Problem
Getting error: **"Please enter a valid Unit Price (EUR)."** when trying to save client orders.

### Root Cause
The validation was trying to parse both the EUR price (user input) AND the TND selling price (auto-calculated) fields. If the auto-calculation didn't run or the selling price field was empty, the validation would fail.

### Solution Applied

#### 1. ✅ Pre-Validation Calculation
```java
// Run calculation before validation to ensure fields are populated
calculatePrices.run();
```

#### 2. ✅ Smart Validation Logic
- Only requires EUR price from user input
- Auto-calculates TND selling price if the field is empty
- Handles both comma (18,50) and period (18.50) decimal formats

#### 3. ✅ Fallback Calculation
```java
if (sellingText == null || sellingText.trim().isEmpty()) {
    // Calculate selling price manually if field is empty
    double calculatedSelling = Double.parseDouble(cleanPrice) * 5.0;
    o.setSellingPrice(calculatedSelling);
}
```

#### 4. ✅ Better Error Messages
- "Please enter a Unit Price (EUR)." for empty field
- "Please enter a valid numeric Unit Price (EUR)." for invalid format

### Expected Behavior Now
✅ Enter EUR price → Auto-calculates TND price  
✅ Click OK → Validation passes even if auto-calc didn't run  
✅ Handles European decimal format (comma separator)  
✅ Clear, specific error messages  

### Test Steps
1. Open Add Order dialog
2. Enter product link: "https://example.com/product"  
3. Enter unit price: "18.50" or "18,50"
4. Click OK → Should save successfully

The validation should now work reliably!
