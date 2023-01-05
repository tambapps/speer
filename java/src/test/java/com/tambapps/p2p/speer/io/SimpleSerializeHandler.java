package com.tambapps.p2p.speer.io;

import com.tambapps.p2p.speer.Speer;
import com.tambapps.p2p.speer.exception.HandshakeFailException;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SimpleSerializeHandler implements Serializer<Map<String, Object>>, Deserializer<Map<String, Object>> {

  private static final Pattern ATTRIBUTE_PATTERN = Pattern.compile("([A-Za-z_]+)\\((\\w+)\\)=(.*)");
  private static final String ATTRIBUTES_START = "ATTRIBUTES_START";
  private static final String ATTRIBUTES_END = "ATTRIBUTES_END";

  @Override
  public Map<String, Object> deserialize(InputStream inputStream) throws IOException {
    return readAttributes(new DataInputStream(inputStream));
  }

  @Override
  public void serialize(Map<String, Object> object, OutputStream outputStream)
      throws IOException {
    writeAttributes(object, new DataOutputStream(outputStream));
  }

  protected Map<String, Object> readAttributes(DataInputStream inputStream) throws IOException {
    if (!ATTRIBUTES_START.equals(inputStream.readUTF())) {
      throw new HandshakeFailException("Peer doesn't handle attributes");
    }
    Map<String, Object> properties = new HashMap<>();
    String data;
    while (!(data = inputStream.readUTF()).equals(ATTRIBUTES_END)) {
      Matcher matcher = ATTRIBUTE_PATTERN.matcher(data);
      if (!matcher.find()) {
        throw new HandshakeFailException("Couldn't read attribute");
      }
      try {
        properties.put(matcher.group(1), parseAttribute(matcher.group(2), matcher.group(3)));
      } catch (IllegalArgumentException e) {
        throw new HandshakeFailException("Malformed attribute value", e);
      }
    }
    return properties;
  }

  // TODO handle lists?
  protected Object parseAttribute(String attributeType, String value) {
    switch (attributeType) {
      case "integer":
        return Integer.parseInt(value);
      case "long":
        return Long.parseLong(value);
      case "byte":
        return Byte.parseByte(value);
      case "short":
        return Short.parseShort(value);
      case "float":
        return Float.parseFloat(value);
      case "double":
        return Double.parseDouble(value);
      case "boolean":
        return Boolean.parseBoolean(value);
      case "character":
        return value.charAt(0);
      case "string":
        return value;
      default:
        throw new IllegalArgumentException("Unhandled type '" + attributeType + "'");
    }
  }

  protected String composeAttribute(String attributeName, Object attributeValue) {
    Class<?> type = attributeValue.getClass();
    String typeString = type == Double.class || type == Float.class || type == Long.class ||
        type == Integer.class || type == Short.class || type == Character.class ||
        type == Byte.class || type == Boolean.class ? type.getSimpleName().toLowerCase(Locale.US) :
        "string";
    String valueString = String.valueOf(attributeValue);
    return String.format("%s(%s)=%s", attributeName, typeString, valueString);
  }

  protected void writeAttributes(Map<String, Object> attributes, DataOutputStream outputStream) throws IOException {
    outputStream.writeUTF(ATTRIBUTES_START);
    for (Map.Entry<String, Object> entry : attributes.entrySet()) {
      outputStream.writeUTF(composeAttribute(entry.getKey(), entry.getValue()));
    }
    outputStream.writeUTF(ATTRIBUTES_END);
  }
}
