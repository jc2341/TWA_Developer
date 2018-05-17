package uk.ac.cam.cares.jps.men.entity;

public class Source implements INamed {

	private String name = "";
	// if a named source such as a producing plant produces more than one product, then for each product an own instance of Source is created
	private Product product = null;
	private boolean nearSea = false;
	
	public Source(String name, Product product) {
		this.name = name;
		this.product = product;
	}
	
	public String getName() {
		return name;
	}

	public Product getProduct() {
		return product;
	}
	
	public boolean isNearSea() {
		return nearSea;
	}

	public void setNearSea(boolean nearSea) {
		this.nearSea = nearSea;
	}
	
	public String toString( ) {
		return "Source[name=" + getName() + ", product=" + getProduct().getName() + ", nearSea = " + isNearSea() + "]";
	}
}
