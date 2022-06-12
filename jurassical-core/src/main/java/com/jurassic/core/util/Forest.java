package com.jurassic.core.util;

import java.util.ArrayList;
import java.util.List;

/**
 * BTree��ɵ�ɭ��,ÿһ��BTree����һ��m�׵�B+��,��ĳһ��B+��keyֵ����,�ͻ������µ�B+��
 * 
 * @author yzhu
 * 
 * @param <K>
 * @param <V>
 */
public class Forest<K extends Comparable<K>, V> {

	protected boolean _threadSafe;// �Ƿ�֧���̰߳�ȫ��,trueΪ�߱��̰߳�ȫ��,falseΪ���߱�
	private final int _m;// ����
	private final List<BTree<K, V>> trees;// B+��
	private ReadWriteLock _lock;// �����д����

	public Forest(boolean safe, int m) {
		this._threadSafe = safe;
		this._m = m;
		this.trees = new ArrayList<>();
		BTree<K, V> tree = new BTree<>(m);
		this.trees.add(tree);// Ĭ�����һ��m�׵�B+��
		if (safe) {
			this._lock = new ReadWriteLock();
		}
	}

	/**
	 * ��ѯ
	 */
	public V search(K key) {
		if (this._threadSafe)
			this._lock.readLock();
		try {
			// ��������ÿһ��B+��
			for (BTree<K, V> tree : this.trees) {
				V val = tree.search(key);
				if (val != null) {
					return val;
				}
			}

			return null;
		} finally {
			if (this._threadSafe)
				this._lock.readUnLock();
		}
	}
	
	public void set(K key, V val){
		if (this._threadSafe)
			this._lock.readLock();
		try {
			// ��������ÿһ��B+��
			for (BTree<K, V> tree : this.trees) {
				if (tree.set(key, val)) {
					return;
				}
			}
		} finally {
			if (this._threadSafe)
				this._lock.readUnLock();
		}

	}

	/**
	 * ���
	 */
	public void insert(K key, V value) {
		if (this._threadSafe)
			this._lock.writeLock();
		try {
			// �����һ��B+����insert
			BTree<K, V> lastTree = this.trees.get(this.trees.size() - 1);
			if (!lastTree.insert(key, value)) {
				// ������һ��������,������һ�������в���
				BTree<K, V> newTree = new BTree<>(_m);
				newTree.insert(key, value);
				this.trees.add(newTree);
			}
		} finally {
			if (this._threadSafe)
				this._lock.writeUnLock();
		}
	}

	/**
	 * ���value,��remove����������Ȼ����key,��ĳЩ�����¿����滻remove����
	 */
	public V clear(K key) {
		if (this._threadSafe)
			this._lock.writeLock();
		try {
			// ���γ�����ÿһ��tree��ɾ��key
			for (BTree<K, V> tree : this.trees) {
				V val = tree.clear(key);
				if (val != null) {
					return val;
				}
			}

			return null;
		} finally {
			if (this._threadSafe)
				this._lock.writeUnLock();
		}
	}

	/**
	 * ���value,���ǻ�һ��ɾ����Ӧ��key,������������Ľṹ
	 */
	public V remove(K key) {
		if (this._threadSafe)
			this._lock.writeLock();
		try {
			// ���γ�����ÿһ��tree��ɾ��key
			for (BTree<K, V> tree : this.trees) {
				V val = tree.remove(key);
				if (val != null) {
					return val;
				}
			}

			return null;
		} finally {
			if (this._threadSafe)
				this._lock.writeUnLock();
		}
	}
}
