/**
 *****************************************************************************
 * Copyright (c) 2017 IBM Corporation and other Contributors.

 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Arpit Rastogi - Initial Contribution
 *****************************************************************************
 */
/*
 * Cloudant DB sample template class of JSON document structure stored in DB.
 */

package com.ibm.watson.WatsonVRTraining.CloudantNoSQLDB;

public class JSonDocumentTemplateClass {
	private String img_base64 = null;
	private String img_id = null;
	private String img_result_html = null;
	private String _rev = null;
	private String random_img_obj_str=null;
	private String _id = null;
	private int score = 0;

	public JSonDocumentTemplateClass(String img_base64, String img_id, String img_result_html, String _rev,
			String random_img_obj_str, String _id, String app_id, int score) {
		super();
		this.img_base64 = img_base64;
		this.img_id = img_id;
		this.img_result_html = img_result_html;
		this._rev = _rev;
		this.random_img_obj_str = random_img_obj_str;
		this._id = _id;
		this.app_id = app_id;
		this.score = score;
	}
	public String getRandom_img_obj_str() {
		return random_img_obj_str;
	}
	public void setRandom_img_obj_str(String random_img_obj_str) {
		this.random_img_obj_str = random_img_obj_str;
	}
	public String getApp_id() {
		return app_id;
	}
	public void setApp_id(String app_id) {
		this.app_id = app_id;
	}
	public int getScore() {
		return score;
	}
	public void setScore(int score) {
		this.score = score;
	}
	private String app_id = null;
	public String get_rev() {
		return _rev;
	}
	public void set_rev(String _rev) {
		this._rev = _rev;
	}
	  public String getImg_result_html() {
		return img_result_html;
	}
	public void setImg_result_html(String img_result_html) {
		this.img_result_html = img_result_html;
	}
	public String getImg_id() {
		return img_id;
	}
	public void setImg_id(String img_id) {
		this.img_id = img_id;
	}
	public String getImg_base64() {
		return img_base64;
	}
	public void setImg_base64(String img_base64) {
		this.img_base64 = img_base64;
	}
	  public String get_id() {
		return _id;
	}
	public void set_id(String _id) {
		this._id = _id;
	}
}
