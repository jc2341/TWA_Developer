package uk.ac.cam.cares.jps.ontomatch.alignment;

import java.util.Iterator;

import org.apache.jena.graph.Triple;
import org.apache.jena.query.ResultSet;

/**
 * Object class
 * Iterator that iterates Jena ResultSet as a list of string array
 *
 * @author shaocong zhang
 * @version 1.0
 * @since 2020-09-08
 */
public class ResultSetStringArrayIterator implements Iterator<String[]> {
	   private ResultSet rs;
	   private QuerySolutionToStringArrayAdapter ad;
	   private Iterator<String[]> it = null;
	   public ResultSetStringArrayIterator(ResultSet resultSet, QuerySolutionToStringArrayAdapter adapter) {
	      this.rs = resultSet;
	      this.ad = adapter;
	   }
	   @Override
	   public boolean hasNext() {
	      if(it != null && it.hasNext()){
	         return true;
	      }
	      it = null;
	      return rs.hasNext();
	   }
	   @Override
	   public String[] next() {
	       if(it == null){
	           it = ad.adapt(rs.next());
	       }
	       return it.next();
	   }
	}
