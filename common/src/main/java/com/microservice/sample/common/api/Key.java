package com.microservice.sample.common.api;

import java.util.Arrays;

/**
 * キーオブジェクト
 * 
 * @author yoshy0407
 *
 */
public class Key {

	private Object[] keys;

	/**
	 * インスタンスを生成します
	 * @param keys キーとなる値
	 */
	public Key(Object...keys) {
		this.keys = keys;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.deepHashCode(keys);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Key other = (Key) obj;
		return Arrays.deepEquals(keys, other.keys);
	}
}
