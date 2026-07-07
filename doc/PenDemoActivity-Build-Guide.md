##### 📖 English | 📖 [中文](PenDemoActivity-Build-Guide_zh.md)

# PenDemoActivity Build Guide

How to add OnyxPenDemo handwriting to your Activity. See `PenDemoActivity.java` for a full example.

---

## 1. Dependencies

```gradle
implementation 'com.onyx.android.sdk:onyxsdk-pen:1.5.4'
implementation 'org.lsposed.hiddenapibypass:hiddenapibypass:4.3'
```

Add the Onyx Maven repository in the root `build.gradle`:

`http://repo.boox.com/repository/maven-public/`

Initialize in `Application.onCreate`:

```java
ResManager.init(this);
RxBaseAction.init(this);
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
    HiddenApiBypass.addHiddenApiExemptions("");
}
```

---

## 2. Activity integration

Use `PenSession` as the handwriting entry point. Bind a `SurfaceView` and forward lifecycle callbacks; you do not need to call `TouchHelper` directly.

```java
private PenSession penSession;

@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.your_layout);

    EpdController.enablePost(getWindow().getDecorView(), 1);

    penSession = PenSession.create(this);
    penSession.initialize();

    SurfaceView surfaceView = findViewById(R.id.surfaceView);
    View excludeOverlay = findViewById(R.id.tool_bar); // optional: non-drawing UI region

    surfaceView.getHolder().addCallback(
            penSession.createSurfaceCallback(surfaceView, excludeOverlay));
    surfaceView.setOnTouchListener((v, e) -> true);
}

@Override protected void onResume() {
    super.onResume();
    penSession.onResume();
}

@Override protected void onPause() {
    penSession.onPause();
    super.onPause();
}

@Override
public void onWindowFocusChanged(boolean hasFocus) {
    super.onWindowFocusChanged(hasFocus);
    penSession.onWindowFocusChanged(hasFocus);
}

@Override protected void onDestroy() {
    penSession.onDestroy();
    super.onDestroy();
}
```

Layout requirements: `SurfaceView` width and height must be greater than 0 at `surfaceCreated`. Pass toolbars and other non-drawing views as `excludeOverlay` so ink does not land on controls.

---

## 3. Pen settings

Change brush parameters through `PenSession` (values are synced to `TouchHelper` internally):

```java
penSession.setTool(PenTool.BRUSH);
penSession.setShapeType(shapeType, texture);
penSession.setStrokeWidthMm(shapeType, widthMm);
penSession.setStrokeColor(color);
```

See `ShapeFactory` for `shapeType` values (e.g. fountain pen `SHAPE_BRUSH_SCRIBBLE`, soft brush `SHAPE_NEO_BRUSH_SCRIBBLE`).

---

## 4. Reading strokes

After pen-up, strokes are stored in `PenManager.drawShape`:

```java
List<Shape> shapes = penSession.getPenManager().getDrawShape();
```

To restore from storage:

```java
penSession.getPenManager().replaceDrawShapes(shapes);
// Redraw on the pen thread; see PenCommands.run
```

---

## 5. Runtime

Must run on a BOOX device that supports Raw Drawing.

For the lower-level `TouchHelper` API, see [Onyx-Pen-SDK](Onyx-Pen-SDK.md).
