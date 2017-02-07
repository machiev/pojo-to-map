# pojo-to-map

Converts between POJO object and map.

# Object to map

There can be created a `Map<String, Object>` from Java object. Created map stores object's fields values.
Map's key is a field's mapping name and map's value is a field's reference. Mapping name is either field's name or a name specified by `@Mapped` annotation.

# Map to object

There can be an object of specified instance created from a map.
