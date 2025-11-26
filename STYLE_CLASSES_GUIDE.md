# Modern Style Classes - Quick Reference Guide

## 🎨 **How to Apply Modern Styling to Your JavaFX Components**

### **Page Layout**
```java
BorderPane view = new BorderPane();
view.getStyleClass().add("page-container"); // Adds modern spacing and background
```

### **Buttons**
```java
Button primaryBtn = new Button("Save");
primaryBtn.getStyleClass().addAll("modern-button", "button-primary"); // Blue primary button

Button secondaryBtn = new Button("Cancel"); 
secondaryBtn.getStyleClass().addAll("modern-button", "button-secondary"); // Gray secondary button

Button deleteBtn = new Button("Delete");
deleteBtn.getStyleClass().addAll("modern-button", "button-error"); // Red error button
```

### **Input Fields**
```java
TextField textField = new TextField();
textField.getStyleClass().add("modern-field"); // Modern input styling

ComboBox<String> comboBox = new ComboBox<>();
comboBox.getStyleClass().add("modern-field"); // Modern dropdown styling

Spinner<Integer> spinner = new Spinner<>();
spinner.getStyleClass().add("modern-field"); // Modern spinner styling
```

### **Tables**
```java
TableView<Order> table = new TableView<>();
table.getStyleClass().add("modern-table"); // Modern table with hover effects
```

### **Containers**
```java
HBox buttonContainer = new HBox();
buttonContainer.getStyleClass().add("action-buttons"); // Proper spacing for buttons

VBox formSection = new VBox();
formSection.getStyleClass().add("form-group"); // Form field grouping
```

### **Tab Navigation**
```java
TabPane tabPane = new TabPane();
tabPane.getStyleClass().add("modern-tabs"); // Professional tab styling
```

## 🎯 **Component-Specific Examples**

### **Dashboard Cards**
```java
VBox statCard = new VBox();
statCard.getStyleClass().add("stat-card"); // Modern dashboard card

Label statValue = new Label("1,234");
statValue.getStyleClass().add("stat-value"); // Large number styling

Label statLabel = new Label("Total Orders");
statLabel.getStyleClass().add("stat-label"); // Subtitle styling
```

### **Status Badges**
```java
Label activeBadge = new Label("Active");
activeBadge.getStyleClass().addAll("badge", "badge-active"); // Green active badge

Label pendingBadge = new Label("Pending");
pendingBadge.getStyleClass().addAll("badge", "badge-pending"); // Orange pending badge

Label inactiveBadge = new Label("Inactive");
inactiveBadge.getStyleClass().addAll("badge", "badge-inactive"); // Red inactive badge
```

### **Search and Filters**
```java
TextField searchField = new TextField();
searchField.getStyleClass().add("modern-field");
searchField.setPromptText("Search...");

HBox searchContainer = new HBox();
searchContainer.getStyleClass().add("action-buttons"); // Proper search bar spacing
```

## 🔧 **Theme Switching**

### **Loading Light Theme:**
```java
scene.getStylesheets().clear();
scene.getStylesheets().addAll(
    getClass().getResource("/ui/app.css").toExternalForm(),
    getClass().getResource("/ui/light-theme.css").toExternalForm()
);
```

### **Loading Dark Theme:**
```java
scene.getStylesheets().clear();
scene.getStylesheets().addAll(
    getClass().getResource("/ui/app.css").toExternalForm(),
    getClass().getResource("/ui/dark-theme.css").toExternalForm()
);
```

## 🎨 **Color Reference**

### **Light Theme Colors:**
- Primary: `#4E54C8` (Professional blue)
- Success: `#4CAF50` (Green)
- Warning: `#FF9800` (Orange) 
- Error: `#F44336` (Red)
- Background: `#F5F5F7` (Light gray)

### **Dark Theme Colors:**
- Primary: `#8F94FB` (Bright purple-blue)
- Success: `#66BB6A` (Light green)
- Warning: `#FFB74D` (Light orange)
- Error: `#EF5350` (Light red)
- Background: `#1E1E2F` (Dark blue-gray)

## 📏 **Spacing Guidelines**

### **Button Groups:**
```java
HBox buttons = new HBox(12); // 12px spacing
buttons.getStyleClass().add("action-buttons");
```

### **Form Fields:**
```java
VBox form = new VBox(16); // 16px vertical spacing
form.getStyleClass().add("form-group");
```

### **Cards:**
```java
VBox card = new VBox(20); // 20px internal spacing
card.getStyleClass().add("stat-card");
```

## 🚀 **Best Practices**

1. **Always apply `page-container`** to main views for consistent spacing
2. **Use `modern-button` as base** then add specific button type (`button-primary`, etc.)
3. **Apply `modern-field`** to all input components for consistency  
4. **Group related buttons** with `action-buttons` container class
5. **Use semantic badge classes** for status indicators

## ✨ **Result**
Following this guide will give you a modern, professional 2025 UI that matches contemporary design standards while maintaining full JavaFX functionality.
