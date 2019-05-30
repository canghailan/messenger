package cc.whohow.messenger;

import com.fasterxml.jackson.databind.node.ObjectNode;

import java.nio.ByteBuffer;

public interface Message {
    String ID = "_id_";
    String TIMESTAMP = "_timestamp_";
    String FROM = "_from_";
    String TO = "_to_";
    String TYPE = "_type_";
    String UID = "_uid_";
    String ERROR = "_error_";
    String DATA = "_data_";

    String getId();

    String getFrom();

    String getTo();

    ObjectNode toJson();

    ByteBuffer toBytes();
}
