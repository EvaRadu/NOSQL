package graph;

import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import com.orientechnologies.orient.core.db.record.OIdentifiable;
import com.orientechnologies.orient.core.db.record.ORecordElement;
import com.orientechnologies.orient.core.exception.ORecordNotFoundException;
import com.orientechnologies.orient.core.exception.OSerializationException;
import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.ODirection;
import com.orientechnologies.orient.core.record.OEdge;
import com.orientechnologies.orient.core.record.ORecord;
import com.orientechnologies.orient.core.record.OVertex;
import com.orientechnologies.orient.core.serialization.OSerializableStream;
import com.orientechnologies.orient.core.storage.OStorage;

import java.util.Optional;
import java.util.Set;

public class Knows implements OEdge {
    @Override
    public OVertex getFrom() {
        return null;
    }

    @Override
    public OVertex getTo() {
        return null;
    }

    @Override
    public boolean isLightweight() {
        return false;
    }

    @Override
    public Set<String> getPropertyNames() {
        return null;
    }

    @Override
    public <RET> RET getProperty(String s) {
        return null;
    }

    @Override
    public boolean hasProperty(String s) {
        return false;
    }

    @Override
    public void setProperty(String s, Object o) {

    }

    @Override
    public void setProperty(String s, Object o, OType... oTypes) {

    }

    @Override
    public <RET> RET removeProperty(String s) {
        return null;
    }

    @Override
    public Optional<OVertex> asVertex() {
        return Optional.empty();
    }

    @Override
    public Optional<OEdge> asEdge() {
        return Optional.empty();
    }

    @Override
    public boolean isVertex() {
        return false;
    }

    @Override
    public boolean isEdge() {
        return false;
    }

    @Override
    public Optional<OClass> getSchemaType() {
        return Optional.empty();
    }

    @Override
    public boolean detach() {
        return false;
    }

    @Override
    public <RET extends ORecord> RET reset() {
        return null;
    }

    @Override
    public <RET extends ORecord> RET unload() {
        return null;
    }

    @Override
    public <RET extends ORecord> RET clear() {
        return null;
    }

    @Override
    public <RET extends ORecord> RET copy() {
        return null;
    }

    @Override
    public ORID getIdentity() {
        return null;
    }

    @Override
    public <T extends ORecord> T getRecord() {
        return null;
    }

    @Override
    public void lock(boolean b) {

    }

    @Override
    public boolean isLocked() {
        return false;
    }

    @Override
    public OStorage.LOCKING_STRATEGY lockingStrategy() {
        return null;
    }

    @Override
    public void unlock() {

    }

    @Override
    public int getVersion() {
        return 0;
    }

    @Override
    public ODatabaseDocument getDatabase() {
        return null;
    }

    @Override
    public boolean isDirty() {
        return false;
    }

    @Override
    public <RET extends ORecord> RET load() throws ORecordNotFoundException {
        return null;
    }

    @Override
    public <RET extends ORecord> RET reload() throws ORecordNotFoundException {
        return null;
    }

    @Override
    public <RET extends ORecord> RET reload(String s, boolean b, boolean b1) throws ORecordNotFoundException {
        return null;
    }

    @Override
    public <RET extends ORecord> RET save() {
        return null;
    }

    @Override
    public <RET extends ORecord> RET save(String s) {
        return null;
    }

    @Override
    public <RET extends ORecord> RET save(boolean b) {
        return null;
    }

    @Override
    public <RET extends ORecord> RET save(String s, boolean b) {
        return null;
    }

    @Override
    public <RET extends ORecord> RET delete() {
        return null;
    }

    @Override
    public <RET extends ORecord> RET fromJSON(String s) {
        return null;
    }

    @Override
    public String toJSON() {
        return null;
    }

    @Override
    public String toJSON(String s) {
        return null;
    }

    @Override
    public int getSize() {
        return 0;
    }

    @Override
    public STATUS getInternalStatus() {
        return null;
    }

    @Override
    public void setInternalStatus(STATUS status) {

    }

    @Override
    public <RET> RET setDirty() {
        return null;
    }

    @Override
    public void setDirtyNoChanged() {

    }

    @Override
    public ORecordElement getOwner() {
        return null;
    }

    @Override
    public byte[] toStream() throws OSerializationException {
        return new byte[0];
    }

    @Override
    public OSerializableStream fromStream(byte[] bytes) throws OSerializationException {
        return null;
    }

    @Override
    public int compareTo(OIdentifiable o) {
        return 0;
    }

    @Override
    public int compare(OIdentifiable o1, OIdentifiable o2) {
        return 0;
    }
}
