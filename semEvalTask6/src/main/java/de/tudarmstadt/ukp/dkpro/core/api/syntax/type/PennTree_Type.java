
/* First created by JCasGen Thu Mar 24 15:20:37 CET 2016 */
package de.tudarmstadt.ukp.dkpro.core.api.syntax.type;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.cas.impl.CASImpl;
import org.apache.uima.cas.impl.FSGenerator;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.impl.FeatureImpl;
import org.apache.uima.cas.Feature;
import org.apache.uima.jcas.tcas.Annotation_Type;

/** 
 * Updated by JCasGen Thu Mar 24 15:20:37 CET 2016
 * @generated */
public class PennTree_Type extends Annotation_Type {
  /** @generated 
   * @return the generator for this type
   */
  @Override
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (PennTree_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = PennTree_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new PennTree(addr, PennTree_Type.this);
  			   PennTree_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new PennTree(addr, PennTree_Type.this);
  	  }
    };
  /** @generated */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = PennTree.typeIndexID;
  /** @generated 
     @modifiable */
  @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("de.tudarmstadt.ukp.dkpro.core.api.syntax.type.PennTree");
 
  /** @generated */
  final Feature casFeat_PennTree;
  /** @generated */
  final int     casFeatCode_PennTree;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getPennTree(int addr) {
        if (featOkTst && casFeat_PennTree == null)
      jcas.throwFeatMissing("PennTree", "de.tudarmstadt.ukp.dkpro.core.api.syntax.type.PennTree");
    return ll_cas.ll_getStringValue(addr, casFeatCode_PennTree);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setPennTree(int addr, String v) {
        if (featOkTst && casFeat_PennTree == null)
      jcas.throwFeatMissing("PennTree", "de.tudarmstadt.ukp.dkpro.core.api.syntax.type.PennTree");
    ll_cas.ll_setStringValue(addr, casFeatCode_PennTree, v);}
    
  
 
  /** @generated */
  final Feature casFeat_TransformationNames;
  /** @generated */
  final int     casFeatCode_TransformationNames;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getTransformationNames(int addr) {
        if (featOkTst && casFeat_TransformationNames == null)
      jcas.throwFeatMissing("TransformationNames", "de.tudarmstadt.ukp.dkpro.core.api.syntax.type.PennTree");
    return ll_cas.ll_getStringValue(addr, casFeatCode_TransformationNames);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setTransformationNames(int addr, String v) {
        if (featOkTst && casFeat_TransformationNames == null)
      jcas.throwFeatMissing("TransformationNames", "de.tudarmstadt.ukp.dkpro.core.api.syntax.type.PennTree");
    ll_cas.ll_setStringValue(addr, casFeatCode_TransformationNames, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	 * @generated
	 * @param jcas JCas
	 * @param casType Type 
	 */
  public PennTree_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_PennTree = jcas.getRequiredFeatureDE(casType, "PennTree", "uima.cas.String", featOkTst);
    casFeatCode_PennTree  = (null == casFeat_PennTree) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_PennTree).getCode();

 
    casFeat_TransformationNames = jcas.getRequiredFeatureDE(casType, "TransformationNames", "uima.cas.String", featOkTst);
    casFeatCode_TransformationNames  = (null == casFeat_TransformationNames) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_TransformationNames).getCode();

  }
}



    