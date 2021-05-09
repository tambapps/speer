package com.tambapps.p2p.speer.handshake;

import com.tambapps.p2p.speer.exception.HandshakeFailException;
import com.tambapps.p2p.speer.Speer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AttributeHandshake implements Handshake {

  private static final Pattern ATTRIBUTE_PATTERN = Pattern.compile("([A-Za-z_]+)\\((\\w+)\\)=(.*)");
  private static final String ATTRIBUTES_START = "ATTRIBUTES_START";
  private static final String ATTRIBUTES_END = "ATTRIBUTES_END";
  public static final String PROTOCOL_VERSION_KEY = "protocol_version";

  private final Map<String, Object> properties;

  public AttributeHandshake(Map<String, Object> properties) {
    this.properties = properties;
  }

  @Override
  public Map<String, Object> apply(DataOutputStream outputStream, DataInputStream inputStream)
      throws IOException {
    writeAttributes(properties, outputStream);
    Map<String, Object> attributes = readAttributes(inputStream);
    validate(attributes);
    return attributes;
  }

  // overridable
  protected void validate(Map<String, Object> properties) throws HandshakeFailException {

  }

  public static Map<String, Object> readAttributes(DataInputStream inputStream) throws IOException {
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

  // TODO handle lists
  static Object parseAttribute(String attributeType, String value) {
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

  static String composeAttribute(String attributeName, Object attributeValue) {
    Class<?> type = attributeValue.getClass();
    String typeString = type == Double.class || type == Float.class || type == Long.class ||
        type == Integer.class || type == Short.class || type == Character.class ||
        type == Byte.class || type == Boolean.class ? type.getSimpleName().toLowerCase(Locale.US) :
        "string";
    String valueString = String.valueOf(attributeValue);
    return String.format("%s(%s)=%s", attributeName, typeString, valueString);
  }

  public static void writeAttributes(Map<String, Object> attributes, DataOutputStream outputStream) throws IOException {
    outputStream.writeUTF(ATTRIBUTES_START);
    outputStream.writeUTF(composeAttribute(PROTOCOL_VERSION_KEY, Speer.VERSION));
    for (Map.Entry<String, Object> entry : attributes.entrySet()) {
      outputStream.writeUTF(composeAttribute(entry.getKey(), entry.getValue()));
    }
    outputStream.writeUTF(ATTRIBUTES_END);
  }

}
