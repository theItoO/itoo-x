package xyz.kumaraswamy.itoox.emulation.environment;

import gnu.mapping.Environment;
import gnu.mapping.Location;
import gnu.mapping.LocationEnumeration;
import gnu.mapping.NamedLocation;
import gnu.mapping.Namespace;
import gnu.mapping.Symbol;
import gnu.mapping.UnboundLocationException;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;

public class InterceptionEnvironment extends Environment {

  private final Environment callback;

  public InterceptionEnvironment(Environment callback) {
    this.callback = callback;
  }

  @Override
  public NamedLocation lookup(Symbol symbol, Object o, int i) {
    return callback.lookup(symbol, o, i);
  }

  @Override
  public NamedLocation getLocation(Symbol symbol, Object o, int i, boolean b) {
    return callback.getLocation(symbol, o, i, b);
  }

  @Override
  public void define(Symbol symbol, Object o, Object o1) {
    callback.define(symbol, o, o1);
  }

  @Override
  public LocationEnumeration enumerateLocations() {
    return callback.enumerateLocations();
  }

  @Override
  public LocationEnumeration enumerateAllLocations() {
    return callback.enumerateAllLocations();
  }

  @Override
  public int getFlags() {
    return callback.getFlags();
  }

  @Override
  public void setFlag(boolean setting, int flag) {
    callback.setFlag(setting, flag);
  }

  @Override
  public boolean getCanDefine() {
    return callback.getCanDefine();
  }

  @Override
  public void setCanDefine(boolean canDefine) {
    callback.setCanDefine(canDefine);
  }

  @Override
  public boolean getCanRedefine() {
    return callback.getCanRedefine();
  }

  @Override
  public void setCanRedefine(boolean canRedefine) {
    callback.setCanRedefine(canRedefine);
  }

  @Override
  public void setLocked() {
    callback.setLocked();
  }

  @Override
  public boolean isBound(Symbol key, Object property) {
    return callback.isBound(key, property);
  }

  @Override
  public Object get(Symbol key, Object property, Object defaultValue) {
    return callback.get(key, property, defaultValue);
  }

  @Override
  public Object get(Symbol sym) {
    Object unb = Location.UNBOUND;
    Object val = get(sym, null, unb);

    if (val == unb) {
      throw new UnboundLocationException(sym);
    } else {
      return val;
    }
  }

  @Override
  public void put(Symbol key, Object property, Object newValue) {
    callback.put(key, property, newValue);
  }

  @Override
  public Location unlink(Symbol key, Object property, int hash) {
    return callback.unlink(key, property, hash);
  }

  @Override
  public Object remove(Symbol key, Object property, int hash) {
    return callback.remove(key, property, hash);
  }

  @Override
  public Namespace defaultNamespace() {
    return callback.defaultNamespace();
  }

  @Override
  public Symbol getSymbol(String name) {
    return callback.getSymbol(name);
  }

  @NotNull
  @Override
  public String toString() {
    return callback.toString();
  }

  @Override
  public String toStringVerbose() {
    return callback.toStringVerbose();
  }

  @Override
  public boolean hasMoreElements(LocationEnumeration locationEnumeration) {
    try {
      Method method = callback.getClass().getMethod("hasMoreElements", LocationEnumeration.class);
      method.setAccessible(true);
      return (boolean) method.invoke(callback, locationEnumeration);
    } catch (Exception e) {
      //noinspection CallToPrintStackTrace
      e.printStackTrace();
      return false;
    }
  }

  @Override
  public NamedLocation addLocation(Symbol symbol, Object o, Location location) {
    return callback.addLocation(symbol, o, location);
  }

  @Override
  public String getName() {
    return callback.getName();
  }

  @Override
  public Object getSymbol() {
    return callback.getSymbol();
  }

  @Override
  public Object getProperty(Object key, Object defaultValue) {
    return callback.getProperty(key, defaultValue);
  }

  @Override
  public synchronized void setProperty(Object key, Object value) {
    callback.setProperty(key, value);
  }

  @Override
  public Object removeProperty(Object key) {
    return callback.removeProperty(key);
  }
}
