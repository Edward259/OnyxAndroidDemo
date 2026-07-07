##### [📖 English](PenDemoActivity-Build-Guide.md) | 📖 中文

# PenDemoActivity 构建指南

说明如何在 Activity 中接入 OnyxPenDemo 手写能力。完整示例见 `PenDemoActivity.java`。

---

## 1. 依赖

```gradle
implementation 'com.onyx.android.sdk:onyxsdk-pen:1.5.4'
implementation 'org.lsposed.hiddenapibypass:hiddenapibypass:4.3'
```

根工程 `build.gradle` 需配置 Onyx Maven：`http://repo.boox.com/repository/maven-public/`。

`Application.onCreate` 中初始化：

```java
ResManager.init(this);
RxBaseAction.init(this);
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
    HiddenApiBypass.addHiddenApiExemptions("");
}
```

---

## 2. Activity 集成

手写入口为 `PenSession`，Activity 绑定 `SurfaceView` 并转发生命周期即可，无需直接调用 `TouchHelper`。

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
    View excludeOverlay = findViewById(R.id.tool_bar); // 可选：不参与书写的控件区域

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

布局要求：`SurfaceView` 在 `surfaceCreated` 时宽高大于 0；工具栏等区域通过 `excludeOverlay` 排除，避免笔迹落在按钮上。

---

## 3. 笔参设置

通过 `PenSession` 修改笔刷参数（内部会同步到 `TouchHelper`）：

```java
penSession.setTool(PenTool.BRUSH);
penSession.setShapeType(shapeType, texture);
penSession.setStrokeWidthMm(shapeType, widthMm);
penSession.setStrokeColor(color);
```

`shapeType` 取值见 `ShapeFactory`（钢笔 `SHAPE_BRUSH_SCRIBBLE`、毛笔 `SHAPE_NEO_BRUSH_SCRIBBLE` 等）。

---

## 4. 读取笔画

抬笔后笔画写入 `PenManager.drawShape`，可通过以下方式获取：

```java
List<Shape> shapes = penSession.getPenManager().getDrawShape();
```

从存储恢复时：

```java
penSession.getPenManager().replaceDrawShapes(shapes);
// 在笔线程重绘，见 PenCommands.run
```

---

## 5. 运行环境

须在支持 Raw Drawing 的 BOOX 设备上运行。

底层 `TouchHelper` API 说明见 [Onyx-Pen-SDK](Onyx-Pen-SDK.md)。
