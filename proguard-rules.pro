# Add any ProGuard configurations specific to this
# extension here.

-injars 'classes/artifacts/itoo_x_jar/itoo-x.jar'
-outjars 'classes/artifacts/itoo_x_jar/itoo-x-progaurd.jar'

-keep public class xyz.kumaraswamy.itoox.** {
    public *;
 }

-keeppackagenames gnu.kawa**, gnu.expr**
-repackageclasses 'io/kumaraswamy/hello'

-optimizationpasses 99
-allowaccessmodification
-mergeinterfacesaggressively

-flattenpackagehierarchy
-dontpreverify

-dontwarn **.**