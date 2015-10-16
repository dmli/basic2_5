package com.ldm.basic.bean;

import java.io.Serializable;

/**
 * Created by ldm on 12-12-13.
 * 客户端私有分页Bean
 */
public class BasePager implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	

	/**
	 * 默认1
	 */
	private int page = 1;

	/**
	 * 默认15条
	 */
	private int limit = 15;

    /**
     * 总跳数
     */
    private int totalCount = 0;
	
	private BasePager(){
	}
	
	/**
	 * 实例化一个MPager对象
	 * @return BasePager
	 */
	public static BasePager newInstance(){
		return new BasePager();
	}
	
	/**
	 * 获取第一页数据，当指针需要重新开始时调用该方法（有reset效果）
	 * @return [0]页数， [1]条数
	 */
	public int[] getFirstPage() {
		return new int[] { page = 1, limit };
	}

	/**
	 * 根据当前MPager的状态继续获取下一页数据
	 * @return [0]页数， [1]条数
	 */
	public int[] getNextPage() {
		return new int[] { ++page, limit };
	}

	/**
	 * 获取当前页信息
	 * @return [0]页数， [1]条数
	 */
	public int[] getNowPage() {
		return new int[] { page, limit };
	}
	
	/**
	 * 这个方法返回数组共有三个属性
	 * @return [0]页数， [1]条数  [2]总页数（如果没有第一次返回0）
	 */
	public int[] getNowPageTwo() {
		return new int[] { page, limit, totalCount };
	}

	/**
	 * 是否存在下一页数据
	 * @param totalCount 总记录条数
	 * @return true = 存在
	 */
	public boolean isNextPage(final int totalCount) {
		return totalCount > page * limit;
	}

    /**
     * 是否存在下一页数据，使用内部总条数计算
     * @return true = 存在
     */
    public boolean isNextPage() {
        return totalCount > page * limit;
    }

	/**
	 * 设置每页多少条
	 * @param limit 条数
	 */
	public void setLimit(int limit) {
		this.limit = limit;
	}

    /**
     * 获取每页条数
     * @return 条数
     */
    public int getLimit(){
        return limit;
    }

    /**
     * 获取总条数
     * @return 条数
     */
    public int getTotalCount() {
        return totalCount;
    }

    /**
     * 设置总条数
     * @param totalCount n
     */
    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }
}
