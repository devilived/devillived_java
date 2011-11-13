package com.devil.count;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Count {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		int[] arr={0,-1,1,-2,2,-3,3,-4,-5,-6,7,4};
		count(6,arr);
	}
	public static void count(int k,int[] arr){
		Integer[] rstArr=new Integer[k-1];
		int rstNeg=0;
		List<Integer> lst=new ArrayList<Integer>(arr.length);
		for(int i:arr){
			lst.add(i);
		}
		
		Collections.sort(lst,new Comparator<Integer>() {
			public int compare(Integer o1, Integer o2) {
				if(Math.abs(o1)>Math.abs(o1)){
					return 1;
				}

				if(Math.abs(o1)<Math.abs(o1)){
					return -1;
				}
				return 0;
			}});

		
		lst.subList(0, k-1).toArray(rstArr);
		List<Integer> lst1=lst.subList(k-1,lst.size());
		
		List<Integer> negList=new ArrayList<Integer>();
		for(int i=0;i<lst1.size();i++){
			if(lst1.get(i)==0){
				lst1.remove(i);
			}else if(lst1.get(i)<0){
				negList.add(lst1.get(i));
			}
			
		}
		
		if(negList.size()%2==1){
			int minPos=0; int minPosIdx=-1;
			int maxNeg=0; int maxNegIdx=-1;
			for(int i=0;i<lst1.size();i++){
				int tmp=lst1.get(i);
				if(tmp<0){
					if(maxNegIdx==-1){
						maxNeg=tmp;
						maxNegIdx=i;
					}else{
						if(maxNeg<tmp){
							maxNeg=tmp;
							maxNegIdx=i;
						}
					}
				}else if(tmp>0){
					if(minPosIdx==-1){
						minPos=tmp;
						minPosIdx=i;
					}else{
						if(minPos>tmp){
							minPos=tmp;
							minPosIdx=i;
						}
					}
				}
			}//end
			rstNeg=maxNeg;
			
			int maxPos=0;int maxPosIdx=-1;
			int minNeg=0;int minNegIdx=-1;
			for(int i=0;i<rstArr.length;i++){
				int tmp=rstArr[i];
				if(tmp>0){
					if(maxPosIdx==-1){
						maxPos=tmp;
						maxPosIdx=i;
					}else if(maxPos<tmp){
						maxPos=tmp;
						maxPosIdx=i;
					}
				}else {
					if(minNegIdx==-1){
						minNeg=tmp;
						minNegIdx=i;
					}else if(minNeg>tmp){
						minNeg=tmp;
						minNegIdx=i;
					}
				}
			}
			
			if(maxPosIdx==-1){//前k-1全部为负
				lst.remove(maxNegIdx);
				rstArr[minNeg]=rstArr[minNeg]*maxNeg;
			}else if(minNegIdx==-1){
				lst.remove(maxNegIdx);
				int minPos1=rstArr[0];
				int minPosIdx1=-1;//前k-1中最小的正数
				for(int i=0;i<rstArr.length;i++){
					if(rstArr[i]<minPos1){
						minPos1=rstArr[i];	
						minPosIdx1=i;
					}
				}
				rstArr[minPosIdx1]=rstArr[minPosIdx1]*maxNeg;				
			}else{//有正有负
				System.out.println(Arrays.toString(rstArr));
				System.out.println(lst1);
				System.out.println(maxNeg+" "+minPos+" "+maxPos+" "+minNeg);
				if(maxNeg*minNeg>minPos*maxPos){
					lst1.set(minPosIdx,minNeg);
					rstArr[minNegIdx]=minPos;
				}else{
					lst1.set(maxNegIdx, maxPos);
					rstArr[maxPosIdx]=maxNeg;
				}
			}
		}
		
		System.out.println("前"+(k-1)+"个人的积分分别是："+Arrays.toString(rstArr));
//		System.out.println(rstNeg);
		int mul=lst1.get(0);
		for(int i=1;i<lst1.size();i++){
			mul*=lst1.get(i);
		}
		System.out.println("第"+k+"个人的牌是："+lst1+",积分为："+mul);
		int all=0;
		
		for(int i:rstArr){
			all+=i;
		}
		all+=mul;
		System.out.println("总积分是："+all);
	}
}
