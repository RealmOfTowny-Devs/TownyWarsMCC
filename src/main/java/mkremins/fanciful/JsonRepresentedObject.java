package mkremins.fanciful;

import com.google.gson.stream.JsonWriter;
import java.io.IOException;

abstract interface JsonRepresentedObject
{
  public abstract void writeJson(JsonWriter paramJsonWriter)
    throws IOException;
}
