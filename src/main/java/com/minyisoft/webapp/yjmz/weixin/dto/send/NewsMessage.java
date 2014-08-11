package com.minyisoft.webapp.yjmz.weixin.dto.send;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NewsMessage extends CustomerServiceMessage {
	private List<Article> articles;

	@Override
	public CustomerServiceMessageType getMessageType() {
		return CustomerServiceMessageType.NEWS;
	}

}
