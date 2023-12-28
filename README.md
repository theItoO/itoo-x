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

## Guidelines

1. Do not use global variables while in the background environment
2. Do not use any user-visible or UI components
3. Use `ItooPreferences` in place of Tiny DB, Tiny DB relies on `SharedPreferences` which will not ensure data synchronization
   across background process.
> TinyDB relies on SharedPreferences, this causes synchronization issues. That means when a value is updated from the
App, it does not necessarily mean the changes will reflect in the background process.
Example:

```java
ItooPreferences preferences = new ItooPreferences(context, "my_namespace");
preferences.write("my_tag", "Hello, World!");
String myTag = (String) preferences.read("my_tag", "Default value");
```
4. Make sure to call `Framework.close()` or `flagEnd()` to avoid problems.
5. Do not ask for permissions in background, as a safe measure, framework blocks all such requests.

## Advanced

The Itoo X framework offers much more APIs and functions.

### 1. Check if app is in Background

```java
if (form instanceof InstanceForm.FormX) {
  // app is in background
}
```

### 2. Call a procedure from Itoo

When your extension is in environment created by Itoo, you can request it to call a procedure.

```java
if (form instanceof InstanceForm.FormX) {
  InstanceForm.FormX formX = (InstanceForm.FormX) form;
  ItooCreator creator = formX.creator;
  creator.startProcedureInvoke("my_procedure", "Argument 1", "Argument 2"...);
}
```

While we are already in background, we must not use `Framework` class. `Framework` class tries to create another environment.
`ItooCreator` is responsible for handling background execution while the `Framework` class just wraps it.

### 3. Querying

If your extension is in background, you can query for execution details like the Screen name, and original context `Context`

```java
ItooCreator creator = formX.creator;

String screenName = creator.refScreen;
Context context = creator.context; // I'm not sure why would you want to do it
```

### 4. Listening to events

While you are in the background, you may want to listen to component's events, this is how you do it:

```java
InstanceForm.FormX formX = (InstanceForm.FormX) form
formX.creator.listener = new InstanceForm.Listener() {
  @Override
  public void event(Component component, String componentName, String eventName, Object... args) {
    // maybe do some action :)
    // formX.creator.startProcedureInvoke(procedure, args);
  }
};
```


### 5. Closing Itoo Framework

Once you are done doing the background work, your extension should call `ItooCreator.flagEnd()`

```java
ItooCreator creator = formX.creator;
creator.flagEnd();
```

`flagEnd()` stops all the background execution and destroys the components.

## Thank you