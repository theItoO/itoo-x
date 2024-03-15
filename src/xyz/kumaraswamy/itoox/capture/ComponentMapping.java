// Copyright (C) 2023 Kumaraswamy B G
// GNU GENERAL PUBLIC LICENSE Version 3, 29 June 2007
// See LICENSE for full details

package xyz.kumaraswamy.itoox.capture;

import com.google.appinventor.components.runtime.Component;
import com.google.appinventor.components.runtime.Form;
import gnu.lists.LList;
import gnu.mapping.SimpleSymbol;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class ComponentMapping {

  private static final String COMPONENT_NAMES_FIELD = "components$Mnto$Mncreate";
  private static boolean mapped = false;

  private static final Map<Component, String> components = new HashMap<>();

  // on demand mapping
  public static void map() throws ReflectiveOperationException {
    if (mapped) {
      // components already mapped
      return;
    }
    Form form = Form.getActiveForm();

    Field componentsField = form.getClass().getField(COMPONENT_NAMES_FIELD);
    LList listComponents = (LList) componentsField.get(form);
    for (Object component : listComponents) {
      LList lList = (LList) component;
      SimpleSymbol symbol = (SimpleSymbol) lList.get(2);
      String componentName = symbol.getName();
      Object value = form.getClass().getField(componentName).get(form);
      if (value instanceof Component) {
        components.put((Component) value, componentName);
      }
    }
    mapped = true;
  }

  public static String getComponentName(Component component) throws ReflectiveOperationException {
    map();
    return components.get(component);
  }
}
