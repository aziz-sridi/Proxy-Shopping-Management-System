package util;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class CurrencyRateFetcher {

    private final HttpClient httpClient;

    public CurrencyRateFetcher(Duration timeout) {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(timeout)
                .build();
    }

    public CurrencyRateFetcher() {
        this(Duration.ofSeconds(10));
    }

    public double fetchLatestRate(String baseCurrency, String targetCurrency) throws IOException {
        String url = String.format("https://api.exchangerate-api.com/v4/latest/%s", baseCurrency);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .timeout(Duration.ofSeconds(10))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new IOException("Unexpected HTTP status: " + response.statusCode());
            }
            return parseRateFromBody(response.body(), targetCurrency);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new IOException("Request interrupted", ie);
        }
    }

    private double parseRateFromBody(String body, String targetCurrency) throws IOException {
        String marker = "\"" + targetCurrency + "\"";
        int idx = body.indexOf(marker);
        if (idx == -1) {
            throw new IOException("Rate for " + targetCurrency + " not found in response");
        }
        int colon = body.indexOf(':', idx);
        if (colon == -1) {
            throw new IOException("Malformed response: missing colon for rate");
        }
        int end = body.indexOf(',', colon);
        if (end == -1) {
            end = body.indexOf('}', colon);
        }
        if (end == -1) {
            throw new IOException("Malformed response: missing terminator for rate value");
        }
        String number = body.substring(colon + 1, end).trim();
        try {
            return Double.parseDouble(number);
        } catch (NumberFormatException nfe) {
            throw new IOException("Unable to parse rate value: " + number, nfe);
        }
    }
}
