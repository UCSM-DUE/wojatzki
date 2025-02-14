
/* First created by JCasGen Thu Apr 21 12:49:34 CEST 2016 */
package curatedTypes;

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
 * Updated by JCasGen Thu Apr 21 12:49:34 CEST 2016
 * @generated */
public class CuratedUnderstandability_Type extends Annotation_Type {
  /** @generated 
   * @return the generator for this type
   */
  @Override
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (CuratedUnderstandability_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = CuratedUnderstandability_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new CuratedUnderstandability(addr, CuratedUnderstandability_Type.this);
  			   CuratedUnderstandability_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new CuratedUnderstandability(addr, CuratedUnderstandability_Type.this);
  	  }
    };
  /** @generated */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = CuratedUnderstandability.typeIndexID;
  /** @generated 
     @modifiable */
  @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("curatedTypes.CuratedUnderstandability");
 
  /** @generated */
  final Feature casFeat_understandability;
  /** @generated */
  final int     casFeatCode_understandability;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getUnderstandability(int addr) {
        if (featOkTst && casFeat_understandability == null)
      jcas.throwFeatMissing("understandability", "curatedTypes.CuratedUnderstandability");
    return ll_cas.ll_getStringValue(addr, casFeatCode_understandability);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setUnderstandability(int addr, String v) {
        if (featOkTst && casFeat_understandability == null)
      jcas.throwFeatMissing("understandability", "curatedTypes.CuratedUnderstandability");
    ll_cas.ll_setStringValue(addr, casFeatCode_understandability, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	 * @generated
	 * @param jcas JCas
	 * @param casType Type 
	 */
  public CuratedUnderstandability_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_understandability = jcas.getRequiredFeatureDE(casType, "understandability", "uima.cas.String", featOkTst);
    casFeatCode_understandability  = (null == casFeat_understandability) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_understandability).getCode();

  }
}



    