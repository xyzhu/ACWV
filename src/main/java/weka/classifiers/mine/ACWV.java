
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



	//// Column 01
	//   double minSup = 0.01;	
	//   double minCon = 1.05;
	//
	////   Column 02
	double minSup = 0.01;	
	double minCon = 1.1;
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



	static long timecost = 0;
	LinkedList m_allTheRules=new LinkedList();
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
			f = new CCFP();
			long t1 = System.currentTimeMillis();
			head = f.buildClassifyNorules(myData, m_onlyClass, minSup, 1, minCon);
			long t2 = System.currentTimeMillis();
			timecost += (t2 - t1);
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
			long t1 = System.currentTimeMillis();

			dPro = f.calculatePro(instance, head, classValue);
			//				System.out.println(dPro[0]+"   "+dPro[1]);
			//				System.out.println("*****************");
			long t2 = System.currentTimeMillis();
			timecost += (t2 - t1);
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
		//			String[] arg ={"-t","tictest.arff"};
		//			runClassifier(new JzhACWV(), arg);

		//			String[] arg1 ={"-t","vehicleout.arff"};
		//			runClassifier(new JzhACWV(), arg1);
		//
		//			String[] arg2 ={"-t","balloons.arff"};
		//			runClassifier(new JzhACWV(), arg2);
		//
		//			String[] arg3 ={"-t","car.arff"};
		//			runClassifier(new JzhACWV(), arg3);

		//			String[] arg4 ={"-t","lenses.arff"};
		//			runClassifier(new JzhACWV(), arg4);
		//			
		//			String[] arg5 ={"-t","tic-tac-toe.arff"};
		//			runClassifier(new JzhACWV(), arg5);
		//			
		//			String[] arg6 ={"-t","ionoout2.arff"};
		//			runClassifier(new JzhACWV(), arg6);
		//			
		//			String[] arg7 ={"-t","pimaout.arff"};
		//			runClassifier(new JzhACWV(), arg7);
		//			
		//			String[] arg8 ={"-t","taeout.arff"};
		//			runClassifier(new JzhACWV(), arg8);
		//			
		//			String[] arg9 ={"-t","habermanout.arff"};
		//			runClassifier(new JzhACWV(), arg9);
		//			
		//			String[] arg10={"-t","glassout.arff"};
		//			runClassifier(new JzhACWV(), arg10);
		//			
		//			String[] arg11={"-t","breastout.arff"};
		//			runClassifier(new JzhACWV(), arg11);

		long t3 = System.currentTimeMillis();
		String[] arg12={"-t","cmcout.arff"};
		runClassifier(new ACWV(), arg12);
		long t4 = System.currentTimeMillis();
		System.out.println(t4-t3);

		//			String[] arg13={"-t","ecoliout.arff"};
		//			runClassifier(new JzhACWV(), arg13);
		//			
		//			String[] arg14={"-t","liverout.arff"};
		//			runClassifier(new JzhACWV(), arg14);
		//			
		//			String[] arg15={"-t","postout.arff"};
		//			runClassifier(new JzhACWV(), arg15);
		//			
		//			String[] arg16={"-t","hypoout2.arff"};
		//			runClassifier(new JzhACWV(), arg16);
		//			
		//			String[] arg17={"-t","yeastout.arff"};
		//			runClassifier(new JzhACWV(), arg17);
		//			
		//			String[] arg18={"-t","autoout.arff"};
		//			runClassifier(new JzhACWV(), arg18);
		//			
		//			String[] arg19={"-t","cleveout.arff"};
		//			runClassifier(new JzhACWV(), arg19);
		//			
		//			String[] arg20={"-t","diabetesout.arff"};
		//			runClassifier(new JzhACWV(), arg20);
		//			
		//			String[] arg21={"-t","heartout.arff"};
		//			runClassifier(new JzhACWV(), arg21);
		//			
		//			String[] arg22={"-t","irisout.arff"};
		//			runClassifier(new JzhACWV(), arg22);
		//			
		//			String[] arg23={"-t","laborout.arff"};
		//			runClassifier(new JzhACWV(), arg23);
		//			
		//			String[] arg24={"-t","led7.arff"};
		//			runClassifier(new JzhACWV(), arg24);
		//			
		//			String[] arg25={"-t","wineout.arff"};
		//			runClassifier(new JzhACWV(), arg25);
		//			
		//			String[] arg26={"-t","zoo.arff"};
		//			runClassifier(new JzhACWV(), arg26);
		//			
		//			String[] arg27={"-t","crxout.arff"};
		//			runClassifier(new JzhACWV(), arg27);

		//			String[] arg28={"-t","vehicleout.arff"};
		//			runClassifier(new JzhACWV(), arg28);
		//			
		//			String[] arg29={"-t","lymph.arff"};
		//			runClassifier(new JzhACWV(), arg29);

		//			String[] arg30={"-t","austraout.arff"};
		//			runClassifier(new JzhACWV(), arg30);
		//			
		//			String[] arg31={"-t","hepatiout.arff"};
		//			runClassifier(new JzhACWV(), arg31);
		//			
		//			String[] arg32={"-t","germanout2.arff"};
		//			runClassifier(new JzhACWV(), arg32);
		//			
		//			String[] arg33={"-t","sickout.arff"};
		//			runClassifier(new JzhACWV(), arg33);
		//			
		//			String[] arg34={"-t","horseout2.arff"};
		//			runClassifier(new JzhACWV(), arg34);
		//			
		//			String[] arg35={"-t","annealout.arff"};
		//			runClassifier(new JzhACWV(), arg35);
		//			
		//			String[] arg36={"-t","sonarout2.arff"};
		//			runClassifier(new JzhACWV(), arg36);

	}
}
