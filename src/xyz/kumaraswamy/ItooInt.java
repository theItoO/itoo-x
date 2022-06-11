package xyz.kumaraswamy;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;
import com.google.appinventor.components.runtime.Form;
import gnu.expr.ModuleMethod;
import gnu.lists.FString;
import gnu.lists.LList;
import gnu.lists.Pair;
import gnu.mapping.SimpleSymbol;
import gnu.mapping.Symbol;
import java.lang.reflect.Field;
import java.util.Map;

public class ItooInt {

  private static final String VARS_FIELD_NAME = "global$Mnvars$Mnto$Mncreate";
  private static final String COMPONENT_NAMES_FIELD = "components$Mnto$Mncreate";

  public static final String PROCEDURE_PREFIX = "p$";

  private final SharedPreferences prefInts;
  private final SharedPreferences prefComponents;

  public ItooInt(Form form, String refScreen) {
    prefInts = getSharedPreference(form, refScreen, 0);
    prefComponents = getSharedPreference(form, refScreen, 1);
  }

  public int getInt(String name) {
    return prefInts.getInt(name, -1);
  }

  public Map<String, ?> getAll() {
    return prefInts.getAll();
  }

  public String getPackageNameOf(String name) {
    return prefComponents.getString(name, "");
  }

  public static void saveIntStuff(Form form, String refScreen) throws Throwable{
    saveIntsNames(form, getSharedPreference(form, refScreen, 0));
    saveComponentNames(form, getSharedPreference(form, refScreen, 1));
  }

  private static void saveComponentNames(Form form, SharedPreferences prefs) throws Throwable {
    Editor editor = prefs.edit();
    Field field = form.getClass().getDeclaredField(COMPONENT_NAMES_FIELD);
    LList lList = (LList) field.get(form);

    for (Object object : lList) {
      // the package name of the component
      Pair right = (Pair) ((Pair) object).getCdr();
      FString packageName = (FString) right.getCar();

      // the name (simple name) for the component
      // like Button1
      Pair pair = ((Pair) right.getCdr());
      SimpleSymbol name = (SimpleSymbol) pair.getCar();

      editor.putString(name.getName(), new String(packageName.data));
    }
    editor.commit();
  }

  private static SharedPreferences getSharedPreference(Form form, String refScreen, int type) {
    return form.getSharedPreferences(
        "ItooInt_" + type + "_" + refScreen, Context.MODE_PRIVATE);
  }

  private static void saveIntsNames(Form form, SharedPreferences prefs)
      throws Throwable {
    Editor editor = prefs.edit();
    Field field = form.getClass().getField(VARS_FIELD_NAME);
    LList variables = (LList) field.get(form);

    for (Object variable : variables) {
      if (LList.Empty.equals(variable)) {
        continue;
      }
      LList asPair = (LList) variable;
      String name = ((Symbol) asPair.get(0)).getName();
      if (name.startsWith(PROCEDURE_PREFIX)) {
        ModuleMethod method = (ModuleMethod)
            ((ModuleMethod) asPair.get(1)).apply0();
        int selector = method.selector;
        Log.d("ItooCreator", "Put(" + name + ", " + selector + ")");
        editor.putInt(name.substring(PROCEDURE_PREFIX.length()), selector);
      }
    }
    editor.commit();
  }
}
