# Code-Scanner
QR/BAR Code Scanner Library to scan QR/BAR codes by Raghubeer Singh Virk

Classes for detecting and parsing bar codes are available in the com.google.android.gms.vision.barcode namespace. The BarcodeDetector class is the main workhorse -- processing Frame objects to return a SparseArray types.

The Barcode type represents a single recognized barcode and its value. In the case of 1D barcode such as UPC codes, this will simply be the number that is encoded in the bar code. This is available in the rawValue property, with the detected encoding type set in the format field.

# Implementation Guide

To get a Git project into your build:

*Step 1.* Add the JitPack repository to your build file 

```java
allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
}
```
*Step 2.* Add the dependency
```java
dependencies {
	        implementation 'com.github.RaghuVirk:Code-Scanner:0.1.0'
	}
```
*Step 3.* Call BarcodeCaptureActivity for result.
```jave
BarcodeCaptureActivity.startScanner(Activity.this);
```
*Step 3.* Override onActivityResult method to get results from BarcodeCaptureActivity.
```jave
  /**
     * Called when an activity you launched exits, giving you the requestCode
     * you started it with, the resultCode it returned, and any additional
     * data from it.  The <var>resultCode</var> will be
     * {@link #RESULT_CANCELED} if the activity explicitly returned that,
     * didn't return any result, or crashed during its operation.
     * <p/>
     * <p>You will receive this call immediately before onResume() when your
     * activity is re-starting.
     * <p/>
     *
     * @param requestCode The integer request code originally supplied to
     *                    startActivityForResult(), allowing you to identify who this
     *                    result came from.
     * @param resultCode  The integer result code returned by the child activity
     *                    through its setResult().
     * @param data        An Intent, which can return result data to the caller
     *                    (various data can be attached to Intent "extras").
     * @see #startActivityForResult
     * @see #createPendingResult
     * @see #setResult(int)
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == BarcodeCaptureActivity.RC_BARCODE_CAPTURE) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    Barcode barcode = data.getParcelableExtra(BarcodeCaptureActivity.BarcodeObject);
                    statusMessage.setText(R.string.barcode_success);
                    barcodeValue.setText(barcode.displayValue);
                    Log.d(TAG, "Barcode read: " + barcode.displayValue);
                } else {
                    statusMessage.setText(R.string.barcode_failure);
                    Log.d(TAG, "No barcode captured, intent data is null");
                }
            } else {
                statusMessage.setText(String.format(getString(R.string.barcode_error),
                        CommonStatusCodes.getStatusCodeString(resultCode)));
            }
        }
        else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }```
    
    # Note
    You may face manifest merge error while compilation due to same string name use below code in your manifest to solve the error :-
    ```java
    tools:replace="android:label"
    ```
    Also use 'NoActionbar' theme with this library
    # Thanks you
