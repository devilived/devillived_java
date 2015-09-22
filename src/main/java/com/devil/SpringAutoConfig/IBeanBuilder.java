package com.devil.SpringAutoConfig;

import org.dom4j.Element;

interface IBeanBuilder{
	public Element buildAutoBean(String fulName, Element root) throws ClassNotFoundException;
}