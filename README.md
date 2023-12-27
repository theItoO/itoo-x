![image.png](public/image.png)

Itoo X is a framework that creates a virtual App Inventor environment for background execution.

## Usage for developers

Example for simple usage of the framework:

1. Add `Framework.create()` in your extension's constructor

```java
public MyExtension(ComponentContainer container) throws Throwable {
  Framework.create();
  ...
}
```

2. Calling your procedure from the background

```java
String screenName = "Screen1"; // name of the screen
Framework.FrameworkResult result = Framework.get(this, screenName);
if (result.success()) {
  Framework framework = result.getFramework();
  Framework.CallResult call = framework.call("myBackgroundProcedure", /* optional arguments */0);
  // handle CallResult
} else {
  // something went wrong
  // result.getThrowable()
}
```

3. After you are done, it's necessary to call `framework.close()`

## Tips and guidelines

1. Do not use global variables in the background
2. Do not use UI components, they do not work
3. Avoid using TinyDB, use Itoo's Store/Fetch property blocks