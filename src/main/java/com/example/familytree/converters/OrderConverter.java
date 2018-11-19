package com.example.familytree.converters;

import com.example.familytree.enums.Order;
import java.beans.PropertyEditorSupport;

/**
 * An Order enum converter.
 * It's used to handle @RequestParams in the controller class.
 */
public class OrderConverter extends PropertyEditorSupport {

	public void setAsText(String text) throws IllegalArgumentException {
		setValue(Order.valueOf(text));
	}

}
