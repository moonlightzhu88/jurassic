package com.jurassic.core.util;

import java.util.Iterator;

/**
 * B+��
 * 
 * B+����ΪҶ�ӽڵ�ͷ�Ҷ�ӽڵ�
 * 
 * ÿ��Ҷ�ӽڵ����洢m����������,���ǳ�֮Ϊm��
 * 
 * ÿ��Ҷ�ӽڵ�����Ӵ洢�˶�Ӧkeyֵ�����ݴ洢λ��(����)
 * 
 * ��Ҷ�ӽڵ�����ӵ��m/2������,���ӵ��m������
 * 
 * ÿ������ָ�����²�ķ�Ҷ�ӽڵ����Ҷ�ӽڵ�
 * 
 * B+��������Ҷ�ӽڵ���ͬһ�������
 * 
 * B+�������нڵ�(Ҷ��/��Ҷ��)�Ĵ洢�ռ���һ��ʼ��ʼ����ʱ����Ѿ�ȫ��ȷ����
 * 
 * @author yzhu
 */
public class BTree<K extends Comparable<K>, V> {

	// B+�����ö��������ʾ�洢����,����������������ַ�ռ��ʾ
	// B+������5����ȵ�����ʾ,
	// m��B+����һ����Դ洢m������Ԫ��
	// �ڶ���洢pow(m,2)������Ԫ��
	// ������洢pow(m,3)������Ԫ��
	// ���Ĳ�洢pow(m,4)������Ԫ��
	// ����2�������ַ�ռ�,һ���洢���е�key,һ���洢Ҷ�ӽڵ��Ӧ��value
	private K[] _keyVm;
	private V[] _valueVm;
	// B+��ÿm��key��Ϊһ����,blockSizes����ÿһ�����ʵ�ʴ��key������
	private int[] _blockSizes;
	private final int _m;// B+���Ľ�
	private final int LEVEL = 4;// ���Ĺ̶�����
	private int _total = 0;// B+����Ҷ�ӽڵ�����
	private int _clearNum = 0;// ͳ��b+�����汻���ֵ�Ľڵ�����,<=total
	private final int _leafCapacity;// Ҷ�ӽڵ������
	private final int _leafBlock;// �ײ�Ҷ�ӽڵ��key�ĵ�һ����
	private final int _leafKeyVm;// ��ײ�Ҷ�ӽڵ��key��������ʼ��ַ

	@SuppressWarnings("unchecked")
	public BTree(int m) {
		this._m = m;
		// ����m��4��B+����Ҫ�Ĵ洢�ռ�����
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

		// ���������ַ�ռ�
		this._keyVm = (K[]) new Comparable[keyCapacity];
		this._valueVm = (V[]) new Object[this._leafCapacity];

		// ��ʼ���������ʹ�����
		this._blockSizes = new int[block];

	}

	/**
	 * ����B+��
	 */
	public void destroy() {
		this._keyVm = null;
		this._valueVm = null;
		this._blockSizes = null;
	}

	/**
	 * ���B+������С��key
	 */
	public K getMinKey() {
		if (this._total == 0)
			return null;

		return this._keyVm[this._leafKeyVm];
	}

	/**
	 * ���B+��������key
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
	 * ���B+����Ҷ�ӽڵ������
	 */
	public int getSize() {
		return this._total;
	}

	/**
	 * ����Ҷ�ӽڵ�block������Ӧ�ķ�Ҷ�ӽڵ������ֵ
	 */
	private void _updateNoLeafKey(int leafBlock) {
		if (this._blockSizes[leafBlock] == 0) {
			int tmp = leafBlock;
			for (int i = 0; i < LEVEL - 1; i++) {
				// ���㵱ǰblock�ĸ�block,�Լ���Ӧ������λ��
				tmp--;
				int recallBlock = tmp / this._m;
				int recallOffset = tmp % this._m;
				this._blockSizes[recallBlock]--;
				tmp = recallBlock;

				if (recallOffset != 0) {
					// ���ÿ���޸ĵ�key���Ƕ�Ӧ��ĵ�һ��Ԫ��,����ݿ�����ǰ����
					break;
				}
			}

			return;
		}

		K firstKey = this._keyVm[leafBlock * this._m];

		int tmp = leafBlock;
		for (int i = 0; i < LEVEL - 1; i++) {
			// ���㵱ǰblock�ĸ�block,�Լ���Ӧ������λ��
			tmp--;
			// ����key����
			this._keyVm[tmp] = firstKey;
			int recallBlock = tmp / this._m;
			int recallOffset = tmp % this._m;
			int newSize = recallOffset + 1;
			if (newSize > this._blockSizes[recallBlock]) {
				this._blockSizes[recallBlock] = newSize;
			}
			tmp = recallBlock;

			if (recallOffset != 0) {
				// ���ÿ���޸ĵ�key���Ƕ�Ӧ��ĵ�һ��Ԫ��,����ݿ�����ǰ����
				break;
			}
		}
	}

	/**
	 * B+����������
	 */
	public boolean insert(K key, V value) {
		if (this._total == this._leafCapacity) {
			// ���Ѿ�����,�����ٲ�����
			return false;
		}

		if (this._total == 0) {
			// ����Ҷ��key�ڵ�ĵ�һ��λ��
			this._keyVm[this._leafKeyVm] = key;
			this._blockSizes[this._leafBlock]++;
			// ���뵽value����ĵ�һ��Ԫ����
			this._valueVm[0] = value;
			// ����key��Ҷ�ӽڵ������
			this._updateNoLeafKey(this._leafBlock);

			this._total++;
			return true;
		}

		// �Ӷ��㿪ʼ�����������,�ҵ�key���Բ����λ��
		int vm = this._searchGE(key);
		if (vm == -1) {
			// ��������keyС�ڵ�ǰ��������key,����뵽Ҷ��key�ڵ�ĵ�һ��λ��
			vm = this._leafKeyVm;
		} else {
			// �Ƚ�һ��key�Ͳ���λ�õ�key�Ƿ���ͬ,�����ֱͬ�Ӽ���value����
			K compared = this._keyVm[vm];
			if (key.compareTo(compared) == 0) {
				int valueVm = vm - this._leafKeyVm;
				this._valueVm[valueVm] = value;
				// ���ʱ��keyֵû���µ�����,����ֱ�ӷ��ؼ���
				return true;
			} else {
				// �����λ����vm�ĺ���һ��
				vm++;
			}
		}

		// ��key,value���뵽��������λ��
		return this._insert(key, value, vm);
	}

	/**
	 * ��key,value���뵽leaf�ڵ�ָ����indexλ��
	 * ���leaf����δ��,��ֻ��Ҫ��key-value���뵽indexλ��,ע�����index=0
	 * �Ҳ����keyС��ԭ����ֵ,����Ҫ����searchPath��·������׷���޸ĵ����е�keyֵ
	 * ���leaf����������,��ʹ����ת������ʹ�ò�ֲ���
	 * ����ת:����Ҷ�ӽڵ�����Ҷ�ӽڵ����������δ��,��Ѳ���keyֵ��ĵ�һ��Ԫ���Ƶ����Ҷ�ӽڵ�����һ��Ԫ��
	 * ����ƶ�����������Ҷ�ӽڵ�ĵ�һ��keyֵ�����仯,�����searchPath��·������׷���޸�����keyֵ
	 * ����ת:����Ҷ�ӽڵ���Ҳ�Ҷ�ӽڵ����������δ��,��Ѳ���keyֵ������һ��Ԫ���Ƶ��Ҳ�Ҷ�ӽڵ�Ŀ�ͷ
	 * ���Ҷ��ұߵ�Ҷ�ӽڵ����׷�ݹ���,׷�ݵĹ���ȡsearchPath��ÿ���ڵ���Ҳ���һ��key
	 * ע��:ÿ������׷�ݵ�ʱ��,ֻ�и��·����ڷ�Ҷ�ӽڵ�ĵ�һ��key,����Ҫ�ٴ�����׷��,���򼴿�ֹͣ׷�ݹ���
	 */
	private boolean _insert(K key, V value, int vm) {
		// ����λ��vm�����¼��ֿ�����:
		// 1.�����λ����һ������ʹ�ÿ�Ŀ���λ��,ֱ�Ӽ��뼴��
		// 2.�����λ����һ������ʹ�ÿ�ķǿ���λ��,�Ҳ���黹û��,ֱ�Ӽ��뼴��
		// 3.�����λ����һ������ʹ�ÿ�ķǿ���λ��,�ҵ�ǰ������,���������,����,���߷���3�ֲ���֮һ
		// 4.�������һ���¿�ĵ�һ��λ��,˵��ǰ��һ�����Ѿ�����,���֮ǰ�Ŀ�����������߷��Ѳ���֮һ
		// ���ݲ���key�������ַ,�����Ӧ�Ŀ�Ϳ��ڵ�ƫ����
		int insertedKeyVm = vm;
		int insertedBlock = insertedKeyVm / this._m;
		int insertedOffset = insertedKeyVm % this._m;
		int insertedValueVm = insertedKeyVm - this._leafKeyVm;

		if (insertedBlock >= this._blockSizes.length
				|| this._blockSizes[insertedBlock] == 0) {
			// ����4
			int leftBlock = insertedBlock - 1;
			int lleftBlock = leftBlock - 1;
			if (lleftBlock >= this._leafBlock
					&& this._blockSizes[lleftBlock] < this._m) {
				// ����leftBlock��
				this._leftRotate(leftBlock, this._m - 1);
				// key���������λ��
				this._keyVm[vm - 1] = key;
				this._valueVm[insertedValueVm - 1] = value;
				// ����insertedBlock��Ȼ����״̬,�򲻱��������
			} else {
				// �ֲ�leftBlock
				boolean split = this._split(leftBlock);
				if (!split) {
					// ��Ϊ��������,�޷����Ҳ���ѿ���
					return false;
				}
				// key����insertedBlock��β��
				insertedKeyVm = insertedBlock * this._m
						+ this._blockSizes[insertedBlock];
				insertedValueVm = insertedKeyVm - this._leafKeyVm;
				this._keyVm[insertedKeyVm] = key;
				this._blockSizes[insertedBlock]++;
				this._valueVm[insertedValueVm] = value;
			}
		} else {
			if (this._blockSizes[insertedBlock] == this._m) {
				// ����3
				int leftBlock = insertedBlock - 1;
				int rightBlock = insertedBlock + 1;
				if (leftBlock >= this._leafBlock
						&& this._blockSizes[leftBlock] < this._m) {
					// ����insertedBlock��
					this._leftRotate(insertedBlock, insertedOffset - 1);
					// key,value����insertedOffset-1��λ��
					this._keyVm[insertedKeyVm - 1] = key;
					this._valueVm[insertedValueVm - 1] = value;
					if (insertedOffset == 1) {
						// ������,key�����˿��ͷ��,��Ҫ����һ��key����,��θ���������������ʱ��û��ִ��
						this._updateNoLeafKey(insertedBlock);
					}
					// ���������Ȼ������״̬
				} else if (rightBlock < this._blockSizes.length
						&& this._blockSizes[rightBlock] > 0
						&& this._blockSizes[rightBlock] < this._m) {
					// ����insertedBlock��
					this._rightRotate(insertedBlock, this._m - insertedOffset);
					// ����key,value
					this._keyVm[insertedKeyVm] = key;
					this._valueVm[insertedValueVm] = value;
					if (insertedOffset == 0) {
						// ��������key�ڿ��ͷ��
						this._updateNoLeafKey(insertedBlock);
					}
				} else {
					// �ֲ�insertedBlock��
					boolean split = this._split(insertedBlock);
					if (!split) {
						// ��Ϊ��������,�޷����Ҳ���ѿ���
						return false;
					}
					// ����ԭ��insertedOffset��λ��������key����insertedBlock�黹�����ұߵĿ�
					if (insertedOffset >= this._blockSizes[insertedBlock]) {
						insertedBlock++;
						insertedOffset -= this._m / 2;
						insertedKeyVm = insertedBlock * this._m + insertedOffset;
						insertedValueVm = insertedKeyVm - this._leafKeyVm;
					}
					// ����Ĳ���ͬ����2
					// ��insertedKeyVm�Ժ��Ԫ�����Ҳ��ƶ�һ��
					int tmpKeyVm = insertedBlock * this._m
							+ this._blockSizes[insertedBlock];
					int tmpValueVm = tmpKeyVm - this._leafKeyVm;
					for (; tmpKeyVm > insertedKeyVm; tmpKeyVm--, tmpValueVm--) {
						this._keyVm[tmpKeyVm] = this._keyVm[tmpKeyVm - 1];
						this._valueVm[tmpValueVm] = this._valueVm[tmpValueVm - 1];
					}
					// ����key,value
					this._keyVm[insertedKeyVm] = key;
					this._blockSizes[insertedBlock]++;
					this._valueVm[insertedValueVm] = value;
					if (insertedOffset == 0) {
						// ���������ǿ�ĵ�һ��Ԫ��,����Ҫ����һ��key������
						this._updateNoLeafKey(insertedBlock);
					}
				}
			} else {
				if (insertedOffset == this._blockSizes[insertedBlock]) {
					// ����1
					// ����key,value
					this._keyVm[insertedKeyVm] = key;
					this._blockSizes[insertedBlock]++;
					this._valueVm[insertedValueVm] = value;
					// ����key��Ҷ�ӽڵ������
					this._updateNoLeafKey(insertedBlock);
				} else {
					// ����2
					// ��insertedKeyVm�Ժ��Ԫ�����Ҳ��ƶ�һ��
					int tmpKeyVm = insertedBlock * this._m
							+ this._blockSizes[insertedBlock];
					int tmpValueVm = tmpKeyVm - this._leafKeyVm;
					for (; tmpKeyVm > insertedKeyVm; tmpKeyVm--, tmpValueVm--) {
						this._keyVm[tmpKeyVm] = this._keyVm[tmpKeyVm - 1];
						this._valueVm[tmpValueVm] = this._valueVm[tmpValueVm - 1];
					}
					// ����key,value
					this._keyVm[insertedKeyVm] = key;
					this._blockSizes[insertedBlock]++;
					this._valueVm[insertedValueVm] = value;
					if (insertedOffset == 0) {
						// ���������ǿ�ĵ�һ��Ԫ��,����Ҫ����һ��key������
						this._updateNoLeafKey(insertedBlock);
					}
				}
			}
		}
		this._total++;// key������һ���µ�ֵ
		return true;
	}

	/**
	 * ��block���ݵȷֳ�2��
	 * ���block�������ʹ�õĿ�,�����Ŀ��������Ҳ��ƶ�һ��
	 */
	private boolean _split(int block) {
		if (block == this._blockSizes.length - 1) {
			// �Ѿ������һ�����,�޷��ٷ�����
			return false;
		}

		int blockNum = 0;
		// ͳ��block�Ҳ�ʹ�õĿ�����
		for (int tmp = block + 1; tmp < this._blockSizes.length; tmp++) {
			if (this._blockSizes[tmp] > 0) {
				blockNum++;
			} else {
				break;
			}
		}

		int addBlock = block + blockNum + 1;
		if (addBlock >= this._blockSizes.length) {
			// �·��ѵĿ��Ѿ�������block�ķ�Χ
			return false;
		}
		if (blockNum > 0) {
			// ��Ҫ��block�Ҳ��blockNum����ͬ�����Ҳ��ƶ�һ���
			int _startVm = (block + 1) * this._m;
			int copyLen = blockNum * this._m;
			System.arraycopy(this._keyVm, _startVm, this._keyVm, _startVm
					+ this._m, copyLen);
			int _startValVm = _startVm - this._leafKeyVm;
			System.arraycopy(this._valueVm, _startValVm, this._valueVm,
					_startValVm + this._m, copyLen);

			// �ƶ�ÿһ��������Լ�key����
			for (int i = 0, tmp = addBlock; i < blockNum; i++, tmp--) {
				this._blockSizes[tmp] = this._blockSizes[tmp - 1];
				this._updateNoLeafKey(tmp);
			}
		}

		// �ճ�����block+1��,�������block��ĺ�һ������
		int tailKeyVm = block * this._m + this._m / 2;
		int tailValueVm = tailKeyVm - this._leafKeyVm;
		int headKeyVm = (block + 1) * this._m;
		int headValueVm = headKeyVm - this._leafKeyVm;
		int moveNum = this._m - this._m / 2;
		for (int i = 0; i < moveNum; i++, tailKeyVm++, tailValueVm++, headKeyVm++, headValueVm++) {
			this._keyVm[headKeyVm] = this._keyVm[tailKeyVm];
			this._valueVm[headValueVm] = this._valueVm[tailValueVm];
		}
		// ����block+1���������key����
		this._blockSizes[block + 1] = moveNum;
		this._updateNoLeafKey(block + 1);
		// ����block�������
		this._blockSizes[block] = this._m - moveNum;

		return true;
	}

	/**
	 * ��Ҷ�ӽڵ��ִ����������
	 * ��block��ĵ�һ��Ԫ���ƶ�����ǰ��һ�����β��,��ʣ�µ�len��Ԫ������һ��
	 */
	private void _leftRotate(int block, int len) {
		int leftBlock = block - 1;
		int tailKeyVm = leftBlock * this._m + this._blockSizes[leftBlock];
		int tailValueVm = tailKeyVm - this._leafKeyVm;
		int headKeyVm = block * this._m;
		int headValueVm = headKeyVm - this._leafKeyVm;
		// ��block�ĵ�һ��Ԫ�ظ��Ƶ�leftBlock�Ķ�β
		this._keyVm[tailKeyVm] = this._keyVm[headKeyVm];
		this._valueVm[tailValueVm] = this._valueVm[headValueVm];
		this._blockSizes[leftBlock]++;

		if (len > 0) {
			// ��ƫ������1��ʼ��len��Ԫ������һ��
			int keyVm = block * this._m + 1;
			int valueVm = keyVm - this._leafKeyVm;
			for (int i = 0; i < len; i++, keyVm++, valueVm++) {
				this._keyVm[keyVm - 1] = this._keyVm[keyVm];
				this._valueVm[valueVm - 1] = this._valueVm[valueVm];
			}
			// ���ڿ��׵�Ԫ�ط����˱仯,��Ҫ����һ��key����
			this._updateNoLeafKey(block);
		}
	}

	/**
	 * ��Ҷ�ӽڵ��ִ����������
	 * ��block������һ��Ԫ���ƶ�����������һ�����ͷ����,��β����len��Ԫ������һ��
	 */
	private void _rightRotate(int block, int len) {
		int rightBlock = block + 1;
		int rightKeyVm = rightBlock * this._m + this._blockSizes[rightBlock];
		int rightValueVm = rightKeyVm - this._leafKeyVm;
		// ��rightBlock�ڵ�Ԫ��ȫ������һ��
		for (int i = 0; i < this._blockSizes[rightBlock] + len; i++, rightKeyVm--, rightValueVm--) {
			this._keyVm[rightKeyVm] = this._keyVm[rightKeyVm - 1];
			this._valueVm[rightValueVm] = this._valueVm[rightValueVm - 1];
		}
		// �Ҳ�Ŀ�������1
		this._blockSizes[rightBlock]++;

		// ����һ��rightBlock��key����
		this._updateNoLeafKey(rightBlock);
	}

	/**
	 * �������ڵ��ڲ���key�ĵ�һ��Ԫ�ص�λ��
	 * ���ش�B+���ĸ���ʼ������·����¼����
	 */
	private int _searchGE(K key) {
		// ��������-1
		if (this._total == 0) {
			return -1;
		}

		// �Ⱥ�������Сkey�Ƚ�һ��,��������key�Ƿ�С�ڵ�ǰ�������нڵ�
		if (key.compareTo(this._keyVm[0]) < 0) {
			return -1;
		}

		// �Ӹ��ĵ�һ�㵽����������·��
		int block = 0;
		int vm = 0;

		for (int i = 0; i < LEVEL; i++) {
			int j = this._blockSizes[block] - 1;
			vm = block * this._m + j;// ���key��Ӧ�������ַ=��*��+����ƫ����
			for (; j >= 0; j--, vm--) {
				// ��key���β����ǰ����
				K compared = this._keyVm[vm];
				if (key.compareTo(compared) >= 0) {
					// key��Ҫ���뵽compared֮��(���ٺ�key��ͬ)
					break;
				}
			}
			// ����jλ�õ�����,�ƶ�����һ����Ҷ�ӽڵ��key��
			block = block * this._m + j + 1;
		}

		// ���һ��ͣ����vm��Ϊ��Ҫ���ص�λ��
		return vm;
	}

	/**
	 * �������ڲ���key��Ԫ�ص�λ��
	 * 
	 * ���ش�B+���ĸ���ʼ������·����¼����
	 */
	private int _searchE(K key) {
		// ��������-1
		if (this._total == 0) {
			return -1;
		}

		// �Ⱥ�������Сkey�Ƚ�һ��,��������key�Ƿ�С�ڵ�ǰ�������нڵ�
		if (key.compareTo(this._keyVm[0]) < 0) {
			return -1;
		}

		// �Ӹ��ĵ�һ�㵽����������·��
		int block = 0;
		int vm;

		for (int i = 0; i < LEVEL; i++) {
			int j = this._blockSizes[block] - 1;
			vm = block * this._m + j;// ���key��Ӧ�������ַ=��*��+����ƫ����
			for (; j >= 0; j--, vm--) {
				// ��key���β����ǰ����
				K compared = this._keyVm[vm];
				int re = key.compareTo(compared);
				if (re > 0) {
					// key���ڶ�Ӧλ�õĽڵ�ֵ,����һ������
					break;
				} else if (re == 0) {
					// keyֵ������,�ӵ�ǰ������ֱ��Ҷ�Ӳ�
					if (block >= this._leafBlock) {
						// �Ѿ���Ҷ�Ӳ�,ֱ�ӷ���key�������ַ
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
			// ����jλ�õ�����,�ƶ�����һ����Ҷ�ӽڵ��key��
			block = block * this._m + j + 1;
		}

		// ���һ��ͣ����vm��Ϊ��Ҫ���ص�λ��
		return -1;
	}

	/**
	 * B+��������
	 * ��ֵ����key
	 */
	public V search(K key) {
		if (this._total == 0)
			return null;

		// ����keyҶ�ӽڵ���С�ڵ���key������key
		int vm = this._searchE(key);
		if (vm == -1) {
			// keyС�ڵ�ǰ��������key
			return null;
		}

		return this._valueVm[vm - this._leafKeyVm];
	}
	
	public boolean set(K key, V val){
		if (this._total == 0)
			return false;
		
		// ����keyҶ�ӽڵ���С�ڵ���key������key
		int vm = this._searchE(key);
		if (vm == -1) {
			// keyС�ڵ�ǰ��������key
			return false;
		}

		this._valueVm[vm - this._leafKeyVm] = val;
		return true;
	}

	/**
	 * �������е�key,��С����
	 */
	public Iterator<K> keyIterator() {
		if (this._total == 0)
			return null;

		return new BTreeKIterator();
	}

	/**
	 * B+������key�ı�����(���̰߳�ȫ)
	 * 
	 * @author yzhu
	 * 
	 */
	private class BTreeKIterator implements Iterator<K> {

		private int block;// ������ָ�����ڵ���ʼҶ�ӿ�
		private int offset;// ��ʼ���ڵ�ƫ����

		private int endBlock;// ������ָ��Ľ���Ҷ�ӿ�
		private int endOffset;// �������ڵ�ƫ����

		public BTreeKIterator() {
			this.block = _leafBlock;
			this.offset = 0;

			// ��Ҷ�ӿ��β����ǰ���ҵ���һ��������Ԫ�صĿ�
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
			// �ж����ڵĿ��Ƿ񳬳���Χ
			if (this.block > this.endBlock) {
				return false;
			}

			if (this.block == this.endBlock && this.offset > this.endOffset) {
				return false;
			}

			// ���β��û�д洢Ԫ��,���block size=0,��ʾû�к���������
			if (_blockSizes[this.block] == 0) {
				return false;
			}

			if (this.offset >= _blockSizes[this.block]) {
				// ƫ����������ǰʵ��ֵ,���Զ��ƶ�����һ�����ʼλ�������ж�
				// ��Ϊ�ڱ�����ʱ��,B+�����ܻᷢ��һЩ�仯,����block size��С��֮ǰ��offset
				this.block++;
				this.offset = 0;
				return this.hasNext();
			}

			return true;
		}

		public K next() {
			int vm = this.block * _m + this.offset;
			K key = _keyVm[vm];

			// ������ָ������һ��
			// ָ���ƶ�����һ��key��Ӧ��λ��
			this.offset++;

			// ָ���ƶ�����β��,ָ����һ����Ŀ�ͷ
			if (this.offset >= _blockSizes[this.block]) {
				this.block++;
				this.offset = 0;
			}

			return key;
		}
	}

	public V remove(K key) {
		// ��������key�Ƿ�������
		int keyVm = this._searchE(key);
		if (keyVm == -1) {
			// key������������
			return null;
		}

		// ���key��Ӧ��value
		V value = this._valueVm[keyVm - this._leafKeyVm];
		this._valueVm[keyVm - this._leafKeyVm] = null;
		if (value == null) {
			// ���֮ǰ��Ӧ��value�Ѿ���clear,��clear������Ҫͬ����1
			this._clearNum--;
		}

		// ���ɾ��value��keyû��������value��,��Ҫɾ�����key
		int block = keyVm / this._m;
		int offset = keyVm % this._m;
		// ��block��offset���������key�����ƶ�һ��
		int vm = keyVm + 1;
		int valVm = vm - this._leafKeyVm;
		for (int i = 0; i < this._blockSizes[block] - 1 - offset; i++, vm++, valVm++) {
			this._keyVm[vm - 1] = this._keyVm[vm];
			this._valueVm[valVm - 1] = this._valueVm[valVm];
		}
		this._blockSizes[block]--;// Ҷ�ӿ��������1
		this._total--;

		if (this._blockSizes[block] > 0) {
			// ɾ����key��,���ڻ�������key,��ÿ鲻��,��Ҫ�Ļ�,����key������
			if (offset == 0) {
				this._updateNoLeafKey(block);
			}

			return value;
		}

		// ɾ��key��,block���Ϊһ���յĿ���,���Ҳ�����ʹ�õĿ�һ�������ƶ�һ��,ͬʱ������Ӧ��key����
		int start = block + 1;
		if (this._blockSizes[start] == 0) {
			// ɾ��key���ڵĿ��Ѿ������һ��ʹ�õĿ���
			// ���ʱ��ֻҪ����һ��block�������(������)
			this._updateNoLeafKey(block);
			return value;
		}
		int end = this._blockSizes.length - 1;
		for (; end >= start; end--) {
			if (this._blockSizes[end] > 0) {
				break;
			}
		}

		// ��start�鵽end�������Ԫ��һ�������ƶ�һ���Ĵ�С
		int _startVm = start * this._m;
		int _startValVm = _startVm - this._leafKeyVm;
		int copyLen = (end - start + 1) * this._m;
		System.arraycopy(this._keyVm, _startVm, this._keyVm, _startVm - this._m,
				copyLen);
		System.arraycopy(this._valueVm, _startValVm, this._valueVm, _startValVm
				- this._m, copyLen);

		// ����block(��)��end-1���������key����ֵ
		for (int i = block; i <= end - 1; i++) {
			this._blockSizes[i] = this._blockSizes[i + 1];
			this._updateNoLeafKey(i);
		}

		// ���end���key����
		this._blockSizes[end] = 0;
		this._updateNoLeafKey(end);
		return value;

	}

	/**
	 * ɾ����ֵk��Ӧ��value
	 */
	public V remove(K key, V value) {
		// ��������key�Ƿ�������
		int keyVm = this._searchE(key);
		if (keyVm == -1) {
			// key������������
			return null;
		}

		// ���key��Ӧ��value
		V compared = this._valueVm[keyVm - this._leafKeyVm];
		boolean find = value.equals(compared);

		if (!find) {
			// B+����û�����key��Ӧ��value
			return null;
		}

		this._valueVm[keyVm - this._leafKeyVm] = null;

		// ���ɾ��value��keyû��������value��,��Ҫɾ�����key
		int block = keyVm / this._m;
		int offset = keyVm % this._m;
		// ��block��offset���������key�����ƶ�һ��
		int vm = keyVm + 1;
		int valVm = vm - this._leafKeyVm;
		for (int i = 0; i < this._blockSizes[block] - 1 - offset; i++, vm++, valVm++) {
			this._keyVm[vm - 1] = this._keyVm[vm];
			this._valueVm[valVm - 1] = this._valueVm[valVm];
		}
		this._blockSizes[block]--;// Ҷ�ӿ��������1
		this._total--;

		if (this._blockSizes[block] > 0) {
			// ɾ����key��,���ڻ�������key,��ÿ鲻��,��Ҫ�Ļ�,����key������
			if (offset == 0) {
				this._updateNoLeafKey(block);
			}

			return value;
		}

		// ɾ��key��,block���Ϊһ���յĿ���,���Ҳ�����ʹ�õĿ�һ�������ƶ�һ��,ͬʱ������Ӧ��key����
		int start = block + 1;
		if (this._blockSizes[start] == 0) {
			// ɾ��key���ڵĿ��Ѿ������һ��ʹ�õĿ���
			// ���ʱ��ֻҪ����һ��block�������(������)
			this._updateNoLeafKey(block);
			return value;
		}
		int end = this._blockSizes.length - 1;
		for (; end >= start; end--) {
			if (this._blockSizes[end] > 0) {
				break;
			}
		}

		// ��start�鵽end�������Ԫ��һ�������ƶ�һ���Ĵ�С
		int _startVm = start * this._m;
		int _startValVm = _startVm - this._leafKeyVm;
		int copyLen = (end - start + 1) * this._m;
		System.arraycopy(this._keyVm, _startVm, this._keyVm, _startVm - this._m,
				copyLen);
		System.arraycopy(this._valueVm, _startValVm, this._valueVm, _startValVm
				- this._m, copyLen);

		// ����block(��)��end-1���������key����ֵ
		for (int i = block; i <= end - 1; i++) {
			this._blockSizes[i] = this._blockSizes[i + 1];
			this._updateNoLeafKey(i);
		}

		// ���end���key����
		this._blockSizes[end] = 0;
		this._updateNoLeafKey(end);
		return value;
	}

	public V[] getValues() {
		return this._valueVm;
	}

	/**
	 * ���key��Ӧvalue,���ǲ�ɾ��valueռ�õ�element
	 * Ҳ���ᷢ�����ṹ�ĵ���
	 */
	public V clear(K key) {
		if (this._total == 0)
			return null;

		// ����keyҶ�ӽڵ���С�ڵ���key������key
		int vm = this._searchE(key);
		if (vm == -1) {
			// keyС�ڵ�ǰ��������key
			return null;
		}

		int idx = vm - this._leafKeyVm;
		V value = this._valueVm[idx];
		this._valueVm[idx] = null;
		this._clearNum++;
		return value;
	}

	/**
	 * �ж�B+�����Ƿ�����Ч��Ԫ��
	 * ͨ��total��clearNum�Ĳ�ֵ�����ж�
	 */
	public boolean hasValidValue() {
		return (this._total > this._clearNum);
	}
}
