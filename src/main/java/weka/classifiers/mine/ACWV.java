
package weka.classifiers.mine;
import weka.classifiers.*;
import weka.core.*;

import java.io.*;
import java.util.*;

public class ACWV extends Classifier
{
	double[] classValue;
	int[] classCount;
	Instances myData;
	FastVector m_hashtables = new FastVector();
	public Instances m_onlyClass;
	int clIndex=0;
	int attNum=0;
	CCFP f;
	int count = 0;
	static int c = 0;
	FastVector head;
	int ruleNumLim = 0;



	//// Column 01
	//   double minSup = 0.01;	
	//   double minCon = 1.05;
	//
	////   Column 02
	//	double minSup = 0.01;	
	//	double minConv = 1.1;
	//
	////   Column 03
	//   double minSup = 0.01;	
	//   double minCon = 1.2;
	//
	////   Column 04
	//   double minSup = 0.01;	
	//   double minCon = 1.5;
	////
	//   Column 05
	//   double minSup = 0.02;	
	//   double minCon = 1.05;
	//
	////   Column 06
	//   double minSup = 0.02;	
	//   double minCon = 1.1;
	//
	////   Column 07
	//   double minSup = 0.02;	
	//   double minCon = 1.2;
	//
	////   Column 08
	//double minSup = 0.02;	
	//double minCon = 1.5;
	//
	////   Column 09
	//   double minSup = 0.05;	
	//   double minCon = 1.05;
	//
	////   Column 10
	//   double minSup = 0.05;	
	//   double minCon = 1.1;
	//
	////   Column 11
	//   double minSup = 0.05;	
	//   double minCon = 1.2;
	//
	////   Column 12
	//   double minSup = 0.05;	
	//   double minCon = 1.5;
	//
	////   Column 13
	//   double minSup = 0.1;	
	//   double minCon = 1.05;
	//
	////   Column 14
	//   double minSup = 0.1;	
	//   double minCon = 1.1;
	//
	////   Column 15
	//   double minSup = 0.1;	
	//   double minCon = 1.2;
	//
	////   Column 16
	//   double minSup = 0.1;	
	//   double minCon = 1.5;
	double minSup, minConv;
	static long recurse;
	public ACWV(double minsup, double minconv, int ruleNumLimit){
		minSup = minsup;
		minConv = minconv;
		ruleNumLim = ruleNumLimit;

	}
	public void buildClassifier (Instances data)throws Exception
	{ 

		double upperBoundMinSupport=1;
		myData = LabeledItemSet.divide(data,false);
		attNum=myData.numAttributes();
		//m_onlyClass contains only the class attribute
		m_onlyClass = LabeledItemSet.divide(data,true);
		clIndex=data.classIndex();//index of the class
		//	 int numClass=m_onlyClass.numDistinctValues(0);//number of classValue
		c++;
		//     classCount=new int[numClass];
		//     double[] clValue=m_onlyClass.attributeToDoubleArray(0);
		//	 classValue=differentiate(clValue);//find all the different class value
		//	 count(clValue);
		if(c>1){
			f = new CCFP(ruleNumLim);
			head = f.buildClassifyNorules(myData, m_onlyClass, minSup, 1, minConv);
			//System.out.println("the time cost of building classfier is :" + timecost);

		}
		classValue = getSupB();
		count = 0;
	}

	public double classifyInstance(Instance instance)
	{
		int l=classValue.length;
		double dPro[]=new double[l];
		//	dPro = newcalculatePro(l,instance);
		if (c > 1){
//			ACWV.recurse = 0;
//			long t3 = System.currentTimeMillis();
			dPro = f.calculatePro(instance, head, classValue);
//			long t4 = System.currentTimeMillis();
//			System.out.print("*"+(t4-t3)+"/"+ACWV.recurse+"*, ");
			//	System.out.println(timecost);
		}
		count++;
		int iMax=findMax(dPro); 
		return iMax;
	}

	private int findMax(double[] d)
	{
		int l=d.length;
		int iMax=0;
		double temp=d[0];
		for(int i=1;i<l;i++)
		{
			if(d[i]>temp)
			{
				iMax=i;
				temp=d[i];
			}
		}
		return iMax;
	}
	public double[] getSupB(){
		int len = (m_onlyClass.attribute(0)).numValues();
		double[] supB = new double[len];
		int[] s = new int[len];
		for (int i = 0; i < len; i++){
			s[i] = 0;
		}
		for (int i = 0; i < m_onlyClass.numInstances(); i++){
			int classlabel=(int)(m_onlyClass.instance(i).value(0));
			s[classlabel] ++;
		}
		for (int i = 0; i < len; i++){
			supB[i] = (double)s[i] / (double)(m_onlyClass.numInstances());
		}
		return supB;
	}

	public static void main(String[] argv){
		String arg[] = new String[2];
		arg[0] = "-t";
		arg[1] = argv[0];
		double mins = Double.valueOf(argv[1]);
		double minc = Double.valueOf(argv[2]);
		int numrule = Integer.valueOf(argv[3]);
		long t1 = System.currentTimeMillis();
		runClassifier(new ACWV(mins,minc,numrule), arg);
		long t2 = System.currentTimeMillis();
		System.out.println(", "+(t2-t1));
	}
//			String d[] = {"vehicleout","balloons","car","lenses","tic-tac-toe","ionoout2",
//					"pimaout","taeout","habermanout","glassout","breastout","cmcout","ecoliout",
//					"liverout","postout","hypoout2","yeastout","autoout","cleveout","diabetesout",
//					"heartout","irisout","laborout","led7","wineout","zoo","crxout","lymph",
//					"austraout","germanout2","sickout","horseout2","annealout","sonarout2"};

}
