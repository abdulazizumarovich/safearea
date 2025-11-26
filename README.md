# SafeArea

[![](https://img.shields.io/maven-central/v/io.github.abdulazizumarovich/safearea?color=green)](https://central.sonatype.com/artifact/io.github.abdulazizumarovich/safearea)

An Android views utility library that simplifies system insets overlap handling when
[edge-to-edge](https://developer.android.com/develop/ui/views/layout/edge-to-edge) is enabled.
[Android&nbsp;15&nbsp;enforced&nbsp;edge-to-edge](https://developer.android.com/about/versions/15/behavior-changes-15#edge-to-edge)
for apps targeting Android&nbsp;15&nbsp;(API&nbsp;level&nbsp;35), but your app could opt-out by setting
[`windowOptOutEdgeToEdgeEnforcement`](https://developer.android.com/reference/android/R.attr#windowOptOutEdgeToEdgeEnforcement)&nbsp;to&nbsp;`true`.
For apps targeting Android&nbsp;16&nbsp;(API&nbsp;level&nbsp;36), `windowOptOutEdgeToEdgeEnforcement`
is deprecated and disabled, and your app can't opt-out of going edge-to-edge.


## Overview

SafeArea provides a simple API for handling system insets when implementing edge-to-edge
either programmatically using `SafeArea.apply(view)` or wrapping a view by `SafeAreaFrame`
which extends `FrameLayout`. It ensures initial&nbsp;(user&nbsp;defined) insets&nbsp;(margin&nbsp;or&nbsp;padding)
of a view is preserved and extended when they are behind of a system bars & display cutouts & keyboards,
so the view remains visible.


## Features

- **System Insets Handling:** Automatically detects and responds to system UI overlaps
- **Flexible Configuration:** Supports different edge configurations
- **Simple Integration:** Single method call for most use cases


## Installation

Add the dependency to your app's `build.gradle` file:

```gradle
dependencies {  
    implementation 'io.github.abdulazizumarovich:safearea:<latest-version>'  
}
```


## Usage

### Basic Implementation

```java
public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Enable edge-to-edge display
        EdgeToEdge.enable(this);

        // Set your layout
        setContentView(R.layout.activity_main);

        // Apply SafeArea to handle system insets
        SafeArea.apply(
            // pass a view to protect it from being overlapped
            // by system bars & display cutouts & keyboards
            findViewById(R.id.scroll_content),
            // define which edges should be protected
            SafeArea.EDGE_ALL,
            // define which inset type to use.
            // scrollable contents always should use padding insets 
            // and setting android:clipToPadding="false" is recommended
            SafeArea.InsetType.PADDING
        );
    }
}
```

### Using SafeAreaFrame (XML-based approach)

For a more declarative approach, you can use `SafeAreaFrame` directly in your XML layouts:

```xml
<abdulaziz.umarovich.safearea.SafeAreaFrame
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    app:edges="all">

    <!-- Your content here -->
    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Content automatically protected by SafeArea" />

</abdulaziz.umarovich.safearea.SafeAreaFrame>
```

The `SafeAreaFrame` automatically applies `SafeArea.apply()` with `EDGE_ALL` and `PADDING` inset type
during initialization. You can customize which edges to handle using the `app:edges` attribute.

#### Notes

The `SafeAreaFrame` extends `FrameLayout` and provides a convenient XML-based way
to apply SafeArea inset handling without requiring programmatic calls to `SafeArea.apply()`.
It uses styled attributes to configure edge handling
and automatically applies the SafeArea logic during view construction.

## Todo

- Ability to change `WindowInsetsCompat.Type` to provide developers flexible API.
- Unit & Integration tests
- More samples and demonstration media


## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.


## License

This project is licensed under the MIT License.
