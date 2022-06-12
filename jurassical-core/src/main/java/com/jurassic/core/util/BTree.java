package com.jurassic.core.util;

import java.util.Iterator;

/**
 * B+树
 * 
 * B+树分为叶子节点和非叶子节点
 * 
 * 每个叶子节点最多存储m个链接数据,我们称之为m阶
 * 
 * 每个叶子节点的链接存储了对应key值的数据存储位置(引用)
 * 
 * 非叶子节点最少拥有m/2个链接,最多拥有m个链接
 * 
 * 每个链接指向了下层的非叶子节点或者叶子节点
 * 
 * B+树的所有叶子节点在同一个层次上
 * 
 * B+树的所有节点(叶子/非叶子)的存储空间再一开始初始化的时候便已经全部确定好
 * 
 * @author yzhu
 */
public class BTree<K extends Comparable<K>, V> {

	// B+树采用定长数组表示存储数据,定长数组采用虚拟地址空间表示
	// B+树采用5层深度的树表示,
	// m阶B+树第一层可以存储m个数据元素
	// 第二层存储pow(m,2)个数据元素
	// 第三层存储pow(m,3)个数据元素
	// 第四层存储pow(m,4)个数据元素
	// 采用2个虚拟地址空间,一个存储所有的key,一个存储叶子节点对应的value
	private K[] _keyVm;
	private V[] _valueVm;
	// B+树每m个key作为一个块,blockSizes保存每一个块的实际存放key的数量
	private int[] _blockSizes;
	private final int _m;// B+树的阶
	private final int LEVEL = 4;// 树的固定层数
	private int _total = 0;// B+树的叶子节点数量
	private int _clearNum = 0;// 统计b+树里面被清空值的节点数量,<=total
	private final int _leafCapacity;// 叶子节点的容量
	private final int _leafBlock;// 底层叶子节点的key的第一个块
	private final int _leafKeyVm;// 最底层叶子节点的key的虚拟起始地址

	@SuppressWarnings("unchecked")
	public BTree(int m) {
		this._m = m;
		// 计算m接4层B+树需要的存储空间容量
		int tmp = 1;
		int keyCapacity = 0;
		int block = 0;
		for (int i = 0; i < LEVEL; i++) {
			block += tmp;
			tmp *= m;
			keyCapacity += tmp;
		}
		this._leafKeyVm = keyCapacity - tmp;
		this._leafBlock = this._leafKeyVm / this._m;
		this._leafCapacity = tmp;

		// 分配虚拟地址空间
		this._keyVm = (K[]) new Comparable[keyCapacity];
		this._valueVm = (V[]) new Object[this._leafCapacity];

		// 初始化各个块的使用情况
		this._blockSizes = new int[block];

	}

	/**
	 * 销毁B+树
	 */
	public void destroy() {
		this._keyVm = null;
		this._valueVm = null;
		this._blockSizes = null;
	}

	/**
	 * 获得B+树上最小的key
	 */
	public K getMinKey() {
		if (this._total == 0)
			return null;

		return this._keyVm[this._leafKeyVm];
	}

	/**
	 * 获得B+树上最大的key
	 */
	public K getMaxKey() {
		if (this._total == 0)
			return null;

		for (int i = this._blockSizes.length - 1; i >= this._leafBlock; i--) {
			if (this._blockSizes[i] > 0) {
				int vm = i * this._m + this._blockSizes[i] - 1;
				return this._keyVm[vm];
			}
		}

		return null;
	}

	/**
	 * 获得B+树中叶子节点的数量
	 */
	public int getSize() {
		return this._total;
	}

	/**
	 * 根据叶子节点block更新相应的非叶子节点的索引值
	 */
	private void _updateNoLeafKey(int leafBlock) {
		if (this._blockSizes[leafBlock] == 0) {
			int tmp = leafBlock;
			for (int i = 0; i < LEVEL - 1; i++) {
				// 计算当前block的父block,以及对应索引的位置
				tmp--;
				int recallBlock = tmp / this._m;
				int recallOffset = tmp % this._m;
				this._blockSizes[recallBlock]--;
				tmp = recallBlock;

				if (recallOffset != 0) {
					// 如果每次修改的key并非对应块的第一个元素,则回溯可以提前结束
					break;
				}
			}

			return;
		}

		K firstKey = this._keyVm[leafBlock * this._m];

		int tmp = leafBlock;
		for (int i = 0; i < LEVEL - 1; i++) {
			// 计算当前block的父block,以及对应索引的位置
			tmp--;
			// 更新key索引
			this._keyVm[tmp] = firstKey;
			int recallBlock = tmp / this._m;
			int recallOffset = tmp % this._m;
			int newSize = recallOffset + 1;
			if (newSize > this._blockSizes[recallBlock]) {
				this._blockSizes[recallBlock] = newSize;
			}
			tmp = recallBlock;

			if (recallOffset != 0) {
				// 如果每次修改的key并非对应块的第一个元素,则回溯可以提前结束
				break;
			}
		}
	}

	/**
	 * B+树插入数据
	 */
	public boolean insert(K key, V value) {
		if (this._total == this._leafCapacity) {
			// 树已经满了,不能再插入了
			return false;
		}

		if (this._total == 0) {
			// 插入叶子key节点的第一个位置
			this._keyVm[this._leafKeyVm] = key;
			this._blockSizes[this._leafBlock]++;
			// 插入到value区域的第一个元素内
			this._valueVm[0] = value;
			// 更新key非叶子节点的索引
			this._updateNoLeafKey(this._leafBlock);

			this._total++;
			return true;
		}

		// 从顶层开始逐层向下搜索,找到key可以插入的位置
		int vm = this._searchGE(key);
		if (vm == -1) {
			// 如果插入的key小于当前树的所有key,则插入到叶子key节点的第一个位置
			vm = this._leafKeyVm;
		} else {
			// 比较一下key和插入位置的key是否相同,如果相同直接加入value即可
			K compared = this._keyVm[vm];
			if (key.compareTo(compared) == 0) {
				int valueVm = vm - this._leafKeyVm;
				this._valueVm[valueVm] = value;
				// 这个时候key值没有新的增加,所以直接返回即可
				return true;
			} else {
				// 插入的位置在vm的后面一个
				vm++;
			}
		}

		// 将key,value插入到搜索到的位置
		return this._insert(key, value, vm);
	}

	/**
	 * 将key,value插入到leaf节点指定的index位置
	 * 如果leaf容量未满,则只需要将key-value插入到index位置,注意如果index=0
	 * 且插入的key小于原来的值,则需要根据searchPath的路径向上追溯修改掉所有的key值
	 * 如果leaf的容量满了,先使用旋转策略再使用拆分策略
	 * 左旋转:插入叶子节点的左侧叶子节点存在且容量未满,则把插入key值后的第一个元素移到左侧叶子节点的最后一个元素
	 * 如果移动操作导致了叶子节点的第一个key值发生变化,则根据searchPath的路径向上追溯修改所有key值
	 * 右旋转:插入叶子节点的右侧叶子节点存在且容量未满,则把插入key值后的最后一个元素移到右侧叶子节点的开头
	 * 并且对右边的叶子节点进行追溯过程,追溯的过程取searchPath的每个节点的右侧下一个key
	 * 注意:每次向上追溯的时候,只有更新发生在非叶子节点的第一个key,则需要再次向上追溯,否则即可停止追溯过程
	 */
	private boolean _insert(K key, V value, int vm) {
		// 插入位置vm有以下几种可能性:
		// 1.插入的位置是一个正在使用块的空闲位置,直接加入即可
		// 2.插入的位置是一个正在使用块的非空闲位置,且插入块还没满,直接加入即可
		// 3.插入的位置是一个正在使用块的非空闲位置,且当前块满了,会采用左旋,右旋,或者分裂3种操作之一
		// 4.插入的是一个新块的第一个位置,说明前面一个块已经满了,则对之前的块采用左旋或者分裂操作之一
		// 根据插入key的虚拟地址,计算对应的块和块内的偏移量
		int insertedKeyVm = vm;
		int insertedBlock = insertedKeyVm / this._m;
		int insertedOffset = insertedKeyVm % this._m;
		int insertedValueVm = insertedKeyVm - this._leafKeyVm;

		if (insertedBlock >= this._blockSizes.length
				|| this._blockSizes[insertedBlock] == 0) {
			// 场景4
			int leftBlock = insertedBlock - 1;
			int lleftBlock = leftBlock - 1;
			if (lleftBlock >= this._leafBlock
					&& this._blockSizes[lleftBlock] < this._m) {
				// 左旋leftBlock块
				this._leftRotate(leftBlock, this._m - 1);
				// key插入块的最后位置
				this._keyVm[vm - 1] = key;
				this._valueVm[insertedValueVm - 1] = value;
				// 由于insertedBlock仍然是满状态,则不变更块数量
			} else {
				// 分拆leftBlock
				boolean split = this._split(leftBlock);
				if (!split) {
					// 因为块用完了,无法向右侧分裂块了
					return false;
				}
				// key插入insertedBlock的尾部
				insertedKeyVm = insertedBlock * this._m
						+ this._blockSizes[insertedBlock];
				insertedValueVm = insertedKeyVm - this._leafKeyVm;
				this._keyVm[insertedKeyVm] = key;
				this._blockSizes[insertedBlock]++;
				this._valueVm[insertedValueVm] = value;
			}
		} else {
			if (this._blockSizes[insertedBlock] == this._m) {
				// 场景3
				int leftBlock = insertedBlock - 1;
				int rightBlock = insertedBlock + 1;
				if (leftBlock >= this._leafBlock
						&& this._blockSizes[leftBlock] < this._m) {
					// 左旋insertedBlock块
					this._leftRotate(insertedBlock, insertedOffset - 1);
					// key,value插入insertedOffset-1的位置
					this._keyVm[insertedKeyVm - 1] = key;
					this._valueVm[insertedValueVm - 1] = value;
					if (insertedOffset == 1) {
						// 左旋后,key插入了块的头部,需要更新一次key索引,这次更新在左旋操作的时候没有执行
						this._updateNoLeafKey(insertedBlock);
					}
					// 块的数量仍然保持满状态
				} else if (rightBlock < this._blockSizes.length
						&& this._blockSizes[rightBlock] > 0
						&& this._blockSizes[rightBlock] < this._m) {
					// 右旋insertedBlock块
					this._rightRotate(insertedBlock, this._m - insertedOffset);
					// 插入key,value
					this._keyVm[insertedKeyVm] = key;
					this._valueVm[insertedValueVm] = value;
					if (insertedOffset == 0) {
						// 如果插入的key在块的头部
						this._updateNoLeafKey(insertedBlock);
					}
				} else {
					// 分拆insertedBlock块
					boolean split = this._split(insertedBlock);
					if (!split) {
						// 因为块用完了,无法向右侧分裂块了
						return false;
					}
					// 根据原来insertedOffset的位置来决定key插入insertedBlock块还是它右边的块
					if (insertedOffset >= this._blockSizes[insertedBlock]) {
						insertedBlock++;
						insertedOffset -= this._m / 2;
						insertedKeyVm = insertedBlock * this._m + insertedOffset;
						insertedValueVm = insertedKeyVm - this._leafKeyVm;
					}
					// 下面的操作同场景2
					// 将insertedKeyVm以后的元素往右侧移动一格
					int tmpKeyVm = insertedBlock * this._m
							+ this._blockSizes[insertedBlock];
					int tmpValueVm = tmpKeyVm - this._leafKeyVm;
					for (; tmpKeyVm > insertedKeyVm; tmpKeyVm--, tmpValueVm--) {
						this._keyVm[tmpKeyVm] = this._keyVm[tmpKeyVm - 1];
						this._valueVm[tmpValueVm] = this._valueVm[tmpValueVm - 1];
					}
					// 插入key,value
					this._keyVm[insertedKeyVm] = key;
					this._blockSizes[insertedBlock]++;
					this._valueVm[insertedValueVm] = value;
					if (insertedOffset == 0) {
						// 如果插入的是块的第一个元素,则需要更新一下key的索引
						this._updateNoLeafKey(insertedBlock);
					}
				}
			} else {
				if (insertedOffset == this._blockSizes[insertedBlock]) {
					// 场景1
					// 插入key,value
					this._keyVm[insertedKeyVm] = key;
					this._blockSizes[insertedBlock]++;
					this._valueVm[insertedValueVm] = value;
					// 更新key非叶子节点的索引
					this._updateNoLeafKey(insertedBlock);
				} else {
					// 场景2
					// 将insertedKeyVm以后的元素往右侧移动一格
					int tmpKeyVm = insertedBlock * this._m
							+ this._blockSizes[insertedBlock];
					int tmpValueVm = tmpKeyVm - this._leafKeyVm;
					for (; tmpKeyVm > insertedKeyVm; tmpKeyVm--, tmpValueVm--) {
						this._keyVm[tmpKeyVm] = this._keyVm[tmpKeyVm - 1];
						this._valueVm[tmpValueVm] = this._valueVm[tmpValueVm - 1];
					}
					// 插入key,value
					this._keyVm[insertedKeyVm] = key;
					this._blockSizes[insertedBlock]++;
					this._valueVm[insertedValueVm] = value;
					if (insertedOffset == 0) {
						// 如果插入的是块的第一个元素,则需要更新一下key的索引
						this._updateNoLeafKey(insertedBlock);
					}
				}
			}
		}
		this._total++;// key增加了一个新的值
		return true;
	}

	/**
	 * 将block数据等分成2块
	 * 如果block块后面有使用的块,则后面的块整块向右侧移动一块
	 */
	private boolean _split(int block) {
		if (block == this._blockSizes.length - 1) {
			// 已经是最后一格块了,无法再分裂了
			return false;
		}

		int blockNum = 0;
		// 统计block右侧使用的块数量
		for (int tmp = block + 1; tmp < this._blockSizes.length; tmp++) {
			if (this._blockSizes[tmp] > 0) {
				blockNum++;
			} else {
				break;
			}
		}

		int addBlock = block + blockNum + 1;
		if (addBlock >= this._blockSizes.length) {
			// 新分裂的块已经超出了block的范围
			return false;
		}
		if (blockNum > 0) {
			// 需要将block右侧的blockNum个块同步往右侧移动一格块
			int _startVm = (block + 1) * this._m;
			int copyLen = blockNum * this._m;
			System.arraycopy(this._keyVm, _startVm, this._keyVm, _startVm
					+ this._m, copyLen);
			int _startValVm = _startVm - this._leafKeyVm;
			System.arraycopy(this._valueVm, _startValVm, this._valueVm,
					_startValVm + this._m, copyLen);

			// 移动每一块的数量以及key索引
			for (int i = 0, tmp = addBlock; i < blockNum; i++, tmp--) {
				this._blockSizes[tmp] = this._blockSizes[tmp - 1];
				this._updateNoLeafKey(tmp);
			}
		}

		// 空出来的block+1块,用来存放block块的后一半数据
		int tailKeyVm = block * this._m + this._m / 2;
		int tailValueVm = tailKeyVm - this._leafKeyVm;
		int headKeyVm = (block + 1) * this._m;
		int headValueVm = headKeyVm - this._leafKeyVm;
		int moveNum = this._m - this._m / 2;
		for (int i = 0; i < moveNum; i++, tailKeyVm++, tailValueVm++, headKeyVm++, headValueVm++) {
			this._keyVm[headKeyVm] = this._keyVm[tailKeyVm];
			this._valueVm[headValueVm] = this._valueVm[tailValueVm];
		}
		// 更新block+1块的数量和key索引
		this._blockSizes[block + 1] = moveNum;
		this._updateNoLeafKey(block + 1);
		// 更新block块的数量
		this._blockSizes[block] = this._m - moveNum;

		return true;
	}

	/**
	 * 将叶子节点块执行左旋操作
	 * 将block块的第一个元素移动到它前面一个块的尾部,将剩下的len个元素左移一格
	 */
	private void _leftRotate(int block, int len) {
		int leftBlock = block - 1;
		int tailKeyVm = leftBlock * this._m + this._blockSizes[leftBlock];
		int tailValueVm = tailKeyVm - this._leafKeyVm;
		int headKeyVm = block * this._m;
		int headValueVm = headKeyVm - this._leafKeyVm;
		// 将block的第一个元素复制到leftBlock的队尾
		this._keyVm[tailKeyVm] = this._keyVm[headKeyVm];
		this._valueVm[tailValueVm] = this._valueVm[headValueVm];
		this._blockSizes[leftBlock]++;

		if (len > 0) {
			// 将偏移量从1开始的len个元素左移一格
			int keyVm = block * this._m + 1;
			int valueVm = keyVm - this._leafKeyVm;
			for (int i = 0; i < len; i++, keyVm++, valueVm++) {
				this._keyVm[keyVm - 1] = this._keyVm[keyVm];
				this._valueVm[valueVm - 1] = this._valueVm[valueVm];
			}
			// 由于块首的元素发生了变化,需要更新一次key索引
			this._updateNoLeafKey(block);
		}
	}

	/**
	 * 将叶子节点块执行右旋操作
	 * 将block块的最后一个元素移动到它后面面一个块的头部部,将尾部的len个元素右移一格
	 */
	private void _rightRotate(int block, int len) {
		int rightBlock = block + 1;
		int rightKeyVm = rightBlock * this._m + this._blockSizes[rightBlock];
		int rightValueVm = rightKeyVm - this._leafKeyVm;
		// 将rightBlock内的元素全部右移一格
		for (int i = 0; i < this._blockSizes[rightBlock] + len; i++, rightKeyVm--, rightValueVm--) {
			this._keyVm[rightKeyVm] = this._keyVm[rightKeyVm - 1];
			this._valueVm[rightValueVm] = this._valueVm[rightValueVm - 1];
		}
		// 右侧的块数量加1
		this._blockSizes[rightBlock]++;

		// 更新一下rightBlock的key索引
		this._updateNoLeafKey(rightBlock);
	}

	/**
	 * 搜索大于等于参数key的第一个元素的位置
	 * 返回从B+树的根开始的搜索路径记录下来
	 */
	private int _searchGE(K key) {
		// 空树返回-1
		if (this._total == 0) {
			return -1;
		}

		// 先和树的最小key比较一次,决定参数key是否小于当前树的所有节点
		if (key.compareTo(this._keyVm[0]) < 0) {
			return -1;
		}

		// 从根的第一层到第五层的搜索路径
		int block = 0;
		int vm = 0;

		for (int i = 0; i < LEVEL; i++) {
			int j = this._blockSizes[block] - 1;
			vm = block * this._m + j;// 获得key对应的虚拟地址=块*阶+块内偏移量
			for (; j >= 0; j--, vm--) {
				// 从key块的尾部向前搜索
				K compared = this._keyVm[vm];
				if (key.compareTo(compared) >= 0) {
					// key需要插入到compared之后(至少和key相同)
					break;
				}
			}
			// 根据j位置的索引,移动到下一个非叶子节点的key块
			block = block * this._m + j + 1;
		}

		// 最后一层停留的vm即为需要返回的位置
		return vm;
	}

	/**
	 * 搜索等于参数key的元素的位置
	 * 
	 * 返回从B+树的根开始的搜索路径记录下来
	 */
	private int _searchE(K key) {
		// 空树返回-1
		if (this._total == 0) {
			return -1;
		}

		// 先和树的最小key比较一次,决定参数key是否小于当前树的所有节点
		if (key.compareTo(this._keyVm[0]) < 0) {
			return -1;
		}

		// 从根的第一层到第五层的搜索路径
		int block = 0;
		int vm;

		for (int i = 0; i < LEVEL; i++) {
			int j = this._blockSizes[block] - 1;
			vm = block * this._m + j;// 获得key对应的虚拟地址=块*阶+块内偏移量
			for (; j >= 0; j--, vm--) {
				// 从key块的尾部向前搜索
				K compared = this._keyVm[vm];
				int re = key.compareTo(compared);
				if (re > 0) {
					// key大于对应位置的节点值,走下一层搜索
					break;
				} else if (re == 0) {
					// key值搜索到,从当前块向下直至叶子层
					if (block >= this._leafBlock) {
						// 已经在叶子层,直接返回key的虚拟地址
						return vm;
					} else {
						block = block * this._m + j + 1;
						while (block < this._leafBlock) {
							block = block * this._m + 1;
						}
						return block * this._m;
					}
				}
			}
			// 根据j位置的索引,移动到下一个非叶子节点的key块
			block = block * this._m + j + 1;
		}

		// 最后一层停留的vm即为需要返回的位置
		return -1;
	}

	/**
	 * B+树的搜索
	 * 等值搜索key
	 */
	public V search(K key) {
		if (this._total == 0)
			return null;

		// 搜索key叶子节点中小于等于key的最大的key
		int vm = this._searchE(key);
		if (vm == -1) {
			// key小于当前树的所有key
			return null;
		}

		return this._valueVm[vm - this._leafKeyVm];
	}
	
	public boolean set(K key, V val){
		if (this._total == 0)
			return false;
		
		// 搜索key叶子节点中小于等于key的最大的key
		int vm = this._searchE(key);
		if (vm == -1) {
			// key小于当前树的所有key
			return false;
		}

		this._valueVm[vm - this._leafKeyVm] = val;
		return true;
	}

	/**
	 * 遍历所有的key,从小到大
	 */
	public Iterator<K> keyIterator() {
		if (this._total == 0)
			return null;

		return new BTreeKIterator();
	}

	/**
	 * B+树所有key的遍历器(非线程安全)
	 * 
	 * @author yzhu
	 * 
	 */
	private class BTreeKIterator implements Iterator<K> {

		private int block;// 遍历器指针所在的起始叶子块
		private int offset;// 起始块内的偏移量

		private int endBlock;// 遍历器指针的截至叶子块
		private int endOffset;// 截至块内的偏移量

		public BTreeKIterator() {
			this.block = _leafBlock;
			this.offset = 0;

			// 从叶子块的尾部往前查找到第一个块内有元素的块
			for (int i = _blockSizes.length - 1; i >= _leafBlock; i--) {
				if (_blockSizes[i] > 0) {
					this.endBlock = i;
					this.endOffset = _blockSizes[i] - 1;
					break;
				}
			}
		}

		public BTreeKIterator(int startBlock, int startOffset, int endBlock,
				int endOffset) {
			this.block = startBlock;
			this.offset = startOffset;

			this.endBlock = endBlock;
			this.endOffset = endOffset;
		}

		public boolean hasNext() {
			// 判断所在的块是否超出范围
			if (this.block > this.endBlock) {
				return false;
			}

			if (this.block == this.endBlock && this.offset > this.endOffset) {
				return false;
			}

			// 块的尾部没有存储元素,因此block size=0,表示没有后续的数据
			if (_blockSizes[this.block] == 0) {
				return false;
			}

			if (this.offset >= _blockSizes[this.block]) {
				// 偏移量超出当前实际值,则自动移动到下一块的起始位置在做判断
				// 因为在遍历的时候,B+树可能会发生一些变化,导致block size会小于之前的offset
				this.block++;
				this.offset = 0;
				return this.hasNext();
			}

			return true;
		}

		public K next() {
			int vm = this.block * _m + this.offset;
			K key = _keyVm[vm];

			// 遍历器指针下移一格
			// 指针移动到下一个key对应的位置
			this.offset++;

			// 指针移动到块尾部,指向下一个块的开头
			if (this.offset >= _blockSizes[this.block]) {
				this.block++;
				this.offset = 0;
			}

			return key;
		}
	}

	public V remove(K key) {
		// 首先搜索key是否在树中
		int keyVm = this._searchE(key);
		if (keyVm == -1) {
			// key不存在于树中
			return null;
		}

		// 获得key对应的value
		V value = this._valueVm[keyVm - this._leafKeyVm];
		this._valueVm[keyVm - this._leafKeyVm] = null;
		if (value == null) {
			// 如果之前对应的value已经被clear,则clear计数需要同步减1
			this._clearNum--;
		}

		// 如果删除value后key没有其他的value了,则要删除这个key
		int block = keyVm / this._m;
		int offset = keyVm % this._m;
		// 将block块offset后面的所有key往左移动一格
		int vm = keyVm + 1;
		int valVm = vm - this._leafKeyVm;
		for (int i = 0; i < this._blockSizes[block] - 1 - offset; i++, vm++, valVm++) {
			this._keyVm[vm - 1] = this._keyVm[vm];
			this._valueVm[valVm - 1] = this._valueVm[valVm];
		}
		this._blockSizes[block]--;// 叶子块的数量减1
		this._total--;

		if (this._blockSizes[block] > 0) {
			// 删除完key后,块内还有其他key,则该块不动,需要的话,调整key的索引
			if (offset == 0) {
				this._updateNoLeafKey(block);
			}

			return value;
		}

		// 删除key后,block块变为一个空的块了,将右侧所有使用的块一起向左移动一块,同时修正对应点key索引
		int start = block + 1;
		if (this._blockSizes[start] == 0) {
			// 删除key所在的块已经是最后一格使用的块了
			// 这个时候只要更新一下block块的索引(空索引)
			this._updateNoLeafKey(block);
			return value;
		}
		int end = this._blockSizes.length - 1;
		for (; end >= start; end--) {
			if (this._blockSizes[end] > 0) {
				break;
			}
		}

		// 将start块到end块的所有元素一起向左移动一格块的大小
		int _startVm = start * this._m;
		int _startValVm = _startVm - this._leafKeyVm;
		int copyLen = (end - start + 1) * this._m;
		System.arraycopy(this._keyVm, _startVm, this._keyVm, _startVm - this._m,
				copyLen);
		System.arraycopy(this._valueVm, _startValVm, this._valueVm, _startValVm
				- this._m, copyLen);

		// 修正block(新)到end-1块的数量和key索引值
		for (int i = block; i <= end - 1; i++) {
			this._blockSizes[i] = this._blockSizes[i + 1];
			this._updateNoLeafKey(i);
		}

		// 清空end块的key索引
		this._blockSizes[end] = 0;
		this._updateNoLeafKey(end);
		return value;

	}

	/**
	 * 删除键值k对应的value
	 */
	public V remove(K key, V value) {
		// 首先搜索key是否在树中
		int keyVm = this._searchE(key);
		if (keyVm == -1) {
			// key不存在于树中
			return null;
		}

		// 获得key对应的value
		V compared = this._valueVm[keyVm - this._leafKeyVm];
		boolean find = value.equals(compared);

		if (!find) {
			// B+树中没有这个key对应的value
			return null;
		}

		this._valueVm[keyVm - this._leafKeyVm] = null;

		// 如果删除value后key没有其他的value了,则要删除这个key
		int block = keyVm / this._m;
		int offset = keyVm % this._m;
		// 将block块offset后面的所有key往左移动一格
		int vm = keyVm + 1;
		int valVm = vm - this._leafKeyVm;
		for (int i = 0; i < this._blockSizes[block] - 1 - offset; i++, vm++, valVm++) {
			this._keyVm[vm - 1] = this._keyVm[vm];
			this._valueVm[valVm - 1] = this._valueVm[valVm];
		}
		this._blockSizes[block]--;// 叶子块的数量减1
		this._total--;

		if (this._blockSizes[block] > 0) {
			// 删除完key后,块内还有其他key,则该块不动,需要的话,调整key的索引
			if (offset == 0) {
				this._updateNoLeafKey(block);
			}

			return value;
		}

		// 删除key后,block块变为一个空的块了,将右侧所有使用的块一起向左移动一块,同时修正对应点key索引
		int start = block + 1;
		if (this._blockSizes[start] == 0) {
			// 删除key所在的块已经是最后一格使用的块了
			// 这个时候只要更新一下block块的索引(空索引)
			this._updateNoLeafKey(block);
			return value;
		}
		int end = this._blockSizes.length - 1;
		for (; end >= start; end--) {
			if (this._blockSizes[end] > 0) {
				break;
			}
		}

		// 将start块到end块的所有元素一起向左移动一格块的大小
		int _startVm = start * this._m;
		int _startValVm = _startVm - this._leafKeyVm;
		int copyLen = (end - start + 1) * this._m;
		System.arraycopy(this._keyVm, _startVm, this._keyVm, _startVm - this._m,
				copyLen);
		System.arraycopy(this._valueVm, _startValVm, this._valueVm, _startValVm
				- this._m, copyLen);

		// 修正block(新)到end-1块的数量和key索引值
		for (int i = block; i <= end - 1; i++) {
			this._blockSizes[i] = this._blockSizes[i + 1];
			this._updateNoLeafKey(i);
		}

		// 清空end块的key索引
		this._blockSizes[end] = 0;
		this._updateNoLeafKey(end);
		return value;
	}

	public V[] getValues() {
		return this._valueVm;
	}

	/**
	 * 清空key对应value,但是不删除value占用的element
	 * 也不会发生树结构的调整
	 */
	public V clear(K key) {
		if (this._total == 0)
			return null;

		// 搜索key叶子节点中小于等于key的最大的key
		int vm = this._searchE(key);
		if (vm == -1) {
			// key小于当前树的所有key
			return null;
		}

		int idx = vm - this._leafKeyVm;
		V value = this._valueVm[idx];
		this._valueVm[idx] = null;
		this._clearNum++;
		return value;
	}

	/**
	 * 判断B+树中是否还有有效的元素
	 * 通过total和clearNum的差值进行判断
	 */
	public boolean hasValidValue() {
		return (this._total > this._clearNum);
	}
}
