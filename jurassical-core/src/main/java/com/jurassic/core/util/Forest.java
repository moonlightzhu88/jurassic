package com.jurassic.core.util;

import java.util.ArrayList;
import java.util.List;

/**
 * BTree组成的森林,每一个BTree都是一个m阶的B+树,当某一个B+树key值满了,就会生成新的B+树
 * 
 * @author yzhu
 * 
 * @param <K>
 * @param <V>
 */
public class Forest<K extends Comparable<K>, V> {

	protected boolean _threadSafe;// 是否支持线程安全性,true为具备线程安全性,false为不具备
	private final int _m;// 阶数
	private final List<BTree<K, V>> trees;// B+树
	private ReadWriteLock _lock;// 互斥读写操作

	public Forest(boolean safe, int m) {
		this._threadSafe = safe;
		this._m = m;
		this.trees = new ArrayList<>();
		BTree<K, V> tree = new BTree<>(m);
		this.trees.add(tree);// 默认添加一个m阶的B+树
		if (safe) {
			this._lock = new ReadWriteLock();
		}
	}

	/**
	 * 查询
	 */
	public V search(K key) {
		if (this._threadSafe)
			this._lock.readLock();
		try {
			// 依次搜索每一个B+树
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
			// 依次搜索每一个B+树
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
	 * 添加
	 */
	public void insert(K key, V value) {
		if (this._threadSafe)
			this._lock.writeLock();
		try {
			// 在最后一个B+树上insert
			BTree<K, V> lastTree = this.trees.get(this.trees.size() - 1);
			if (!lastTree.insert(key, value)) {
				// 如果最后一个树满了,则新增一棵树进行插入
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
	 * 清除value,和remove区别在于仍然保留key,在某些场景下可以替换remove操作
	 */
	public V clear(K key) {
		if (this._threadSafe)
			this._lock.writeLock();
		try {
			// 依次尝试在每一个tree中删除key
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
	 * 清除value,但是会一并删除对应的key,会调整整颗树的结构
	 */
	public V remove(K key) {
		if (this._threadSafe)
			this._lock.writeLock();
		try {
			// 依次尝试在每一个tree中删除key
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
