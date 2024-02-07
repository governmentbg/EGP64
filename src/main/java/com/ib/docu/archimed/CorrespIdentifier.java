package com.ib.docu.archimed;

public class CorrespIdentifier {

	public enum Types {
		Unknown, Pin, Pfn, Uic
	}

	private Types	type;
	private String	value;

	public CorrespIdentifier() {
	}

	public CorrespIdentifier(Types type, String value) {
		this.type = type;
		this.value = value;
	}

	public Types getType() {
		return this.type;
	}

	public String getValue() {
		return this.value;
	}

	public void setType(Types type) {
		this.type = type;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
