

/* First created by JCasGen Wed Mar 16 10:14:08 CET 2011 */
package org.u_compare.shared.semantic.bio;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;
import org.u_compare.shared.semantic.NamedEntity;


/** 
 * Updated by JCasGen Tue Mar 06 16:28:20 CET 2012
 * XML source: /vol/home-vol3/wbi/wbi_stud/VLKB/Tim/vlkb/chemspotVSoscar3/desc/cr/iobCR.xml
 * @generated */
public class DNA extends NamedEntity {
  /** @generated
   * @ordered 
   */
  public final static int typeIndexID = JCasRegistry.register(DNA.class);
  /** @generated
   * @ordered 
   */
  public final static int type = typeIndexID;
  /** @generated  */
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected DNA() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated */
  public DNA(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public DNA(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated */  
  public DNA(JCas jcas, int begin, int end) {
    super(jcas);
    setBegin(begin);
    setEnd(end);
    readObject();
  }   

  /** <!-- begin-user-doc -->
    * Write your own initialization here
    * <!-- end-user-doc -->
  @generated modifiable */
  private void readObject() {}
     
}

    