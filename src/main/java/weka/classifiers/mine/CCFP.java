/*
 *    This program is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    alo0ng with this program; if not, write to the Free Software
 *    Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

/*
 *    Apriori.java
 *    Copyright (C) 1999 Eibe Frank,Mark Hall, Stefan Mutter
 *
 */

package weka.classifiers.mine;

import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

import java.util.*;
import java.io.*;

public class CCFP implements Serializable{

	/** for serialization */
	static final long serialVersionUID = 3277498842319212687L;
	protected long timecost;
	protected long numRules;
	/** The minimum support. */
	protected double m_minSupport;
	protected double m_minConv;
	protected long maxRules;
	/** The upper bound on the support */
	protected double m_upperBoundMinSupport;

	/** The lower bound for the minimum support. */
	protected double m_lowerBoundMinSupport;

	/** The maximum number of rules that are output. */
	protected int m_numRules;
	protected boolean terminal ;
	/** The instances (transactions) to be used for generating 
      the association rules. */
	protected Instances m_instances;

	/** Only the class attribute of all Instances.*/
	protected Instances m_onlyClass;

	/** The class index. */  
	protected int m_classIndex;

	public CCFP(int ruleNumLim){
		maxRules = ruleNumLim;
	}
	private CMARtree buildCMARtree(Instances instances,Instances OnlyClass,FastVector head) throws Exception{
		int total=instances.numInstances();
		int numClass = OnlyClass.attribute(0).numValues();

		CMARtree fp=new CMARtree(numClass);
		for (int ii = 0; ii < total; ii++) {
			TNode t = fp.root;
			int classlabel = (int)OnlyClass.instance(ii).value(0);
			int[] sup = new int[numClass];
			for ( int i = 0; i < numClass; i++){
				if (i == classlabel){
					sup[i] = 1;
				}
				else{
					sup[i] = 0;
				}
			}
			for(int j = 0;j < head.size(); j++){
				ListHead set=(ListHead)head.elementAt(j);
				if(set.containedBy(instances.instance(ii))){
					TNode tt = new TNode(set.attr,set.value);
					tt.m_counter = 1;
					tt.sup = new int[numClass];
					for(int jj = 0; jj < sup.length; jj++)
					{
						tt.sup[jj] = sup[jj];
					}
					if(t.child.size() == 0){
						t.addChild(tt);
						t=tt;
						set.addNext(tt);
					}
					else{
						int flag = 1;
						ListIterator<TNode> nodeiter = t.child.listIterator();
						while (nodeiter.hasNext()){
							TNode node = nodeiter.next();
							if (set.equal(node)){
								flag=0;
								t = node;
								t.m_counter++;
								t.sup[classlabel] ++;
								break;
							}
						}

						if(flag > 0){
							t.addChild(tt);
							t = tt;
							set.addNext(tt);
						}

					}
				}
			}
			sup = null;
		}
		return fp;
	}

	public int getHashcode(int a,int v){
		int result = 0;
		if (a < 0)
			return -1;
		for(int j = 0; j < a; j++){				  
			result += m_instances.attribute(j).numValues();
		}
		result += v;
		return result;	  	  
	}
	public int[] getItem(int code){
		int hashcode = code;
		int len = m_instances.numAttributes();
		int[] item = new int[2];
		int i = 0;
		// int value = 0;
		for(i = 0; i < len; i++){
			int num = m_instances.attribute(i).numValues();
			if (hashcode < num)
				break;
			else{
				hashcode -= num;
			}
		}
		item[0] = i;
		item[1] = hashcode;
		return item;

	}

	//keep
	private CMARtree buildCFPtree(FastVector instances,FastVector head) throws Exception{

		int size = instances.size();
		int numClass = m_onlyClass.attribute(0).numValues();
		CMARtree fp=new CMARtree(numClass);
		for (int ii = 0; ii < size; ii++) {
			TNode t=fp.root;
			LabelItemSet instance = (LabelItemSet)instances.elementAt(ii);
			int[] sup = instance.m_sup;
			int headsize = head.size();
			for(int j = 0; j < headsize; j++){
				ListHead set= (ListHead)head.elementAt(j);
				if(set.containedBy(instance)){
					TNode tt=new TNode(set.attr,set.value);
					tt.m_counter=instance.m_counter;
					tt.sup = new int[sup.length];
					for(int jj = 0; jj < sup.length; jj++)
					{
						tt.sup[jj] = sup[jj];
					}
					if(t.child.size() == 0){
						t.addChild(tt);
						t=tt;
						set.addNextII(tt);
					}
					else{
						int flag=1;
						for(int jj=0;jj<t.child.size();jj++){
							TNode node = (TNode)t.child.get(jj);
							if (set.equal(node)){
								flag=0;
								t = node;
								t.m_counter += instance.m_counter;
								for(int k = 0; k < instance.m_sup.length; k++){
									t.sup[k] = t.sup[k]+instance.m_sup[k];
									set.sup[k] = set.sup[k] + instance.m_sup[k];
								}

								break;
							}
						}
						if(flag > 0){
							t.addChild(tt);
							t=tt;
							set.addNextII(tt);
						}
					}
				}
			}
		}
		return fp;
	}

	public FastVector getISet(Instances instances, Instances m_onlyClass) throws Exception{
		FastVector fs = new FastVector();
		//	 int len = ins.numInstances();
		int numClass = m_onlyClass.attribute(0).numValues();
		for (int i = 0; i < instances.numInstances(); i++) {
			Instance ins = instances.instance(i);
			Instance insclass = m_onlyClass.instance(i);
			for (int j = 0; j < instances.numAttributes();j++){
				if (instances.attribute(j).isNumeric())
					throw new Exception("Can't handle numeric attributes!");
				ListHead lh = new ListHead();
				lh.count = 1;
				lh.attr = j;
				lh.value = (int)ins.value(j);
				if (lh.sup == null)
					lh.sup = new int[numClass];
				int classlabel = (int)insclass.value(0); 
				lh.sup[classlabel] = 1;
				if (fs.size() == 0)
					fs.addElement(lh);
				else{
					boolean hasNode = false;
					for (int k =0; k < fs.size(); k++){
						ListHead lk = (ListHead)fs.elementAt(k);
						if (lk.equal(lh))
						{
							hasNode = true;
							lk.count ++;
							lk.sup[classlabel]++;
							break;
						}
					}
					if (hasNode == false)
					{
						fs.addElement(lh);
					}
				}
			}
		}
		return fs;
	}

	public FastVector buildClassifyNorules(Instances instances,Instances onlyClass,double min,double max,double minConv)throws Exception{

		m_instances = instances;
		m_onlyClass = onlyClass;
		m_minSupport = min;
		m_upperBoundMinSupport = max;
		m_minConv = minConv;

		FastVector  kSets;
		int necSupport, necMaxSupport;     


		double nextMinSupport = m_minSupport*(double)m_instances.numInstances();
		double nextMaxSupport = m_upperBoundMinSupport*(double)m_instances.numInstances();
		if((double)Math.rint(nextMinSupport) == nextMinSupport){
			necSupport = (int) nextMinSupport;
		}
		else{
			necSupport = Math.round((float)(nextMinSupport+0.5));
		}
		if((double)Math.rint(nextMaxSupport) == nextMaxSupport){
			necMaxSupport = (int) nextMaxSupport;
		}
		else{
			necMaxSupport = Math.round((float)(nextMaxSupport+0.5));
		}


		//      kSets = ListHead.singleton(m_instances);       
		// 	  ListHead.upDateCounters(kSets,m_instances,m_onlyClass); 
		kSets = getISet(m_instances,m_onlyClass);
		kSets = ListHead.deleteItemSets(kSets, necSupport, necMaxSupport);  

		int size=kSets.size();

		for(int j = 0; j < size; j++){
			for(int k = j+1;k < size; k++){
				int nj=((ListHead)kSets.elementAt(j)).count;
				int nk=((ListHead)kSets.elementAt(k)).count;
				if( nj < nk){  
					kSets.swap(j, k);
				}
			}
		}

		CMARtree fp =buildCMARtree(m_instances,m_onlyClass,kSets);
		int nodecount[] = new int[1];
		printtree(fp.root,nodecount);
		return kSets;
	}
	private void printtree(TNode tnode, int nodecount[]) {
		if(tnode==null)
			return;
		else{
			TNode tn;
			Iterator it = tnode.child.iterator();
			while(it.hasNext()){
				tn = (TNode) it.next();
				nodecount[0]++;
				//System.out.println(tn.father.attr+"   "+tn.father.value+"***"+tn.attr+"   "+tn.value +"   "+tn.sup[0]+"   "+tn.sup[1]);
				printtree(tn, nodecount);
			}


		}

	}

	//keep
	public double[] calculatePro(Instance toTest,FastVector head,double[] supB){
		terminal  = false;
		numRules = 0;
		int len = m_instances.numAttributes();
		int numClass = (m_onlyClass.attribute(0)).numValues();
		int total=m_instances.numInstances();
		int numAttr = 0;
		double[] pro = new double[numClass];
		for(int j = 0; j < len; j++){
			numAttr += m_instances.attribute(j).numValues();
		}
		int min,max;
		double nextMinSupport = m_minSupport*(double)m_instances.numInstances();
		double nextMaxSupport = m_upperBoundMinSupport*(double)m_instances.numInstances();
		if((double)Math.rint(nextMinSupport) == nextMinSupport){
			min = (int) nextMinSupport;
		}
		else{
			min = Math.round((float)(nextMinSupport+0.5));
		}
		if((double)Math.rint(nextMaxSupport) == nextMaxSupport){
			max = (int) nextMaxSupport;
		}
		else{
			max = Math.round((float)(nextMaxSupport+0.5));
		}
		int size = head.size();
		for (int j = (size - 1); j >= 0; j--){
			ListHead lj = (ListHead)head.elementAt(j);
			if(!lj.containedBy(toTest)){
				continue;
			}
			TNode b=new TNode(lj.attr,lj.value);
			b.sup = lj.sup;
			b.m_counter = lj.count;
			boolean nofit = true;
			for (int cc = 0; cc < b.sup.length;cc++){
				if (b.sup[cc] > min){   // new changed
					nofit = false;

					double conf = (double)b.sup[cc] / (double)b.m_counter;
					if (conf == 1)
						conf = 0.999;
					double conv = ( 1 - supB[cc]) / (1 - conf);
					if (conv >= m_minConv){
						numRules++;
						if (numRules > maxRules)
							terminal = true;
						double weight = calWeight(conv,1,len);
						pro[cc] += weight;
					}	 
				}
			}
			if (nofit)
				continue;
			int[] table = new int[numAttr];
			FastVector list=new FastVector();  
			int nextnum = lj.nextnum; 
			for (int l = 0;l < nextnum; l++){ 
				LabelItemSet setl = new LabelItemSet(total,numClass);  
				FastVector nextList = lj.next;
				TNode tl0=(TNode)nextList.elementAt(l);
				setl.m_sup = tl0.sup;
				setl.m_items = new int[len];

				for(int ll = 0; ll <setl.m_items.length; ll++){
					setl.m_items[ll] = -1;
				}
				setl.m_counter = tl0.m_counter;
				TNode t=tl0.father;
				while(t.father!=null){
					if ( t.containedBy(toTest)){
						int index = getHashcode(t.attr,t.value);
						if (index > -1)
							table[index] += tl0.m_counter;
						setl.m_items[t.attr] = t.value;                   // form Cond Pattens 			                                			   
					}
					t=t.father;
				}
				if(setl.size()>0){
					list.addElement(setl);
				}
			}
			if(list.size() > 0){

				FastVector CpTlist=new FastVector();//the list head of new cond-patten tree
				for (int cc = 0; cc < numAttr; cc++){
					if (table[cc] >= min){
						int[] av = getItem(cc);
						ListHead lh = new ListHead(table[cc],av[0],av[1]);
						lh.sup = new int[numClass];
						CpTlist.addElement(lh);

					}
				}
				table = null;
				if(CpTlist.size()==0){
					continue;
				}

				int CpTsize=CpTlist.size();
				for(int x = 0; x < CpTsize; x++){ 			 
					for(int xx = x+1; xx < CpTsize; xx++){
						ListHead tempC=(ListHead)CpTlist.elementAt(x);
						ListHead tempD=(ListHead)CpTlist.elementAt(xx);
						if(tempD.count > tempC.count){
							CpTlist.swap(x, xx);
						}
					} 
				}

				///////// cond-Fp tree build and mine   			 
				try{
					CMARtree subTree = buildCFPtree(list,CpTlist); 

					LinkedList<TNode> beta = new LinkedList<TNode>();
					beta.add(b);
					if(subTree.root.child.size() > 0){
						mineCMARtree(subTree,CpTlist,beta,min,max,supB,pro);  
						if (terminal){
							//							System.out.println("the final number of rules :"+numRules);
							return pro;
						}
					}
				}catch( Exception e){
					e.printStackTrace();
				}
			}


		}
		//		System.out.println("the final number of rules :"+numRules);

		return pro; 
	} 

	/*
	 * if the conditional ccfp-tree has only one path
	 * @param ksets the conditional header table
	 * @param sizealpha the length of the prefix
	 * 
	 */
	 
	private  void mineSinglePath(FastVector ksets,int sizealpha,int itemsize,int supsize,int min,double[] supB,double[] dPro){


		int len = ksets.size();          
		int i = 0; 	
		for (i = 0; i < len; i++){
			ListHead t = (ListHead)ksets.elementAt(i);
			boolean flag = true;;
			for (int k = 0; k < supsize; k++){

				if (t.sup[k] > min){// = new changed
					double conf = (double)t.sup[k] / (double)t.count;
					if (conf == 1)
						conf = 0.999;
					double conv = ( 1 - supB[k]) / ( 1 - conf);
					if (conv >= m_minConv){
						flag = false;
						for (int j = 0; j < (i+1);j++){
							int ruleLength = j+1+sizealpha;
							//			    			
							double weight = calWeight(conv, ruleLength ,itemsize);
							int temp = cal(i,j);
							dPro[k] += weight *  temp ;
							numRules += temp;
							if (numRules > maxRules){
								terminal = true;
								return;
							}
						}

					}
				}
			}
			if (flag){
				break;
			}
		}


		return ;
	}
	
	//calculate how many situations that selet n from m
	private int cal(int m,int n){
		double result = 1;
		if (n == 0)
			return (int)result;
		else{

			double mm = m;
			double nn;
			if (m-n > n){
				nn = n;
			}
			else
				nn = m - n;
			while (nn > 0){
				result = result * ( mm / nn) ;
				mm--;
				nn--;
			}
		}
		return (int)result;

	}
	
	/*
	 * ccfp-growth
	 * @param fp the built CCFP-tree
	 * @param head the header table
	 * @param alpha the prefix
	 */
	private void mineCMARtree(CMARtree fp,FastVector head,LinkedList<TNode> alpha,int min,int max,double[] supB,double[] dPro) throws Exception{

//		ACWV.recurse++;
		int i = 0;	 
		int numAttr=m_instances.numAttributes();       
		int numInstance = m_instances.numInstances();
		int numClass = m_onlyClass.attribute(0).numValues();
		int numAttrValue = 0;
		for(int j = 0; j < numAttr; j++){
			numAttrValue += m_instances.attribute(j).numValues();
		}
		TNode t0=fp.root;      

		if(t0.child.size() == 1){   ////////is or not single path  
			TNode t=(TNode)t0.child.get(0);  
			i++;      
			while(t.child.size() == 1){ 
				i++;
				t=(TNode)t.child.get(0);
			}
		} 
		if (i == head.size()){       //single path
			mineSinglePath(head,alpha.size(),numAttr,numClass,min,supB,dPro);
			return;
		}

		if(t0.child.size() > 0){
			int newsize = head.size();
			for(int j = (newsize -1); j >= 0; j--){

				ListHead lj= (ListHead)head.elementAt(j);
				TNode b=new TNode(lj.attr,lj.value);
				b.sup = lj.sup;
				b.m_counter = lj.count;
				boolean nofit = true;
				for (int cc = 0; cc < b.sup.length;cc++){

					if (b.sup[cc] > min){//=  new changed

						nofit = false;
						double conf = (double)b.sup[cc] / (double)b.m_counter;
						if (conf == 1)
							conf = 0.999;
						double conv = ( 1 - supB[cc]) / (1 - conf);
						if (conv >= m_minConv){
							numRules++;
							if (numRules > maxRules)
							{
								terminal = true;	 
								return;
							}
							int rulelen = alpha.size() + 1;
							double weight = calWeight(conv, rulelen, numAttr);
							dPro[cc] += weight;	
						}

					}
				}
				if (nofit)
					continue;
				int[] table = new int[numAttrValue];
				FastVector list=new FastVector();  
				int nextnum = lj.nextnum; 

				for (int l = 0;l < nextnum; l++){ 
					LabelItemSet setl = new LabelItemSet(numInstance,numClass);  
					FastVector nextList = lj.next;
					TNode tl0=(TNode)nextList.elementAt(l);
					setl.m_sup = tl0.sup;
					setl.m_items = new int[numAttr];

					for(int ll = 0; ll <setl.m_items.length; ll++){
						setl.m_items[ll] = -1;
					}
					setl.m_counter = tl0.m_counter;
					TNode t=tl0.father;
					while(t.father!=null){
						int index = getHashcode(t.attr,t.value);
						if (index > -1)
							table[index] += tl0.m_counter;

						setl.m_items[t.attr] = t.value;                   // form Cond Pattens 			                                			   
						t=t.father;
					}
					if(setl.size()>0){
						list.addElement(setl);
					}
				}
				if(list.size() > 0){

					FastVector CpTlist=new FastVector();//the list head of new cond-patten tree
					for (int cc = 0; cc < numAttrValue; cc++){
						if (table[cc] >= min){
							int[] av = getItem(cc);
							ListHead lh = new ListHead(table[cc],av[0],av[1]);
							lh.sup = new int[numClass];
							CpTlist.addElement(lh);

						}
					}
					table = null;
					if(CpTlist.size()==0){
						continue;
					}

					int CpTsize=CpTlist.size();
					for(int x = 0; x < CpTsize; x++){ 			 
						for(int xx = x+1; xx < CpTsize; xx++){
							ListHead tempC=(ListHead)CpTlist.elementAt(x);
							ListHead tempD=(ListHead)CpTlist.elementAt(xx);
							if(tempD.count > tempC.count){
								CpTlist.swap(x, xx);
							}
						} 
					}

					///////// cond-Fp tree build and mine
					LinkedList<TNode> beta = new LinkedList<TNode>();
					beta.addAll(alpha);
					beta.add(b);
					CMARtree subTree = buildCFPtree(list,CpTlist);
					list = null;
					if(subTree.root.child.size() > 0){
						mineCMARtree(subTree,CpTlist,beta,min,max,supB,dPro);  
						if (terminal)
							return;
					}

				} 


			}
		}


		return ;
	}

	/** the method for calculating the weight
	 * 
	 * @param conv  the confidence of the rule
	 * @param rulelen   the length of the rule without the classlabel
	 * @param size  the size of an instance without the classlabel
	 */
	private double calWeight( double conv, int rulelen, int size){
		double weight = 0;
		//	 weight = conv;
		double d = size - rulelen;
		if (d == 0)
			d = 0.01;
		weight = conv /d;
		//	 weight =  conv* rulelen;

		//	 weight = conv * rulelen / size;
		//	 weight = conv * (rulelen+1) /(size+1);
		return weight;
	}

}

