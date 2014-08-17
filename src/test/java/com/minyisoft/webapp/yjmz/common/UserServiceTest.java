package com.minyisoft.webapp.yjmz.common;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.minyisoft.webapp.yjmz.common.service.UserService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:root-context.xml")
public class UserServiceTest {
	@Autowired
	private UserService userService;

	@Test
	public void testUserQuery() {
		Assert.assertNotNull(userService.getCollection());
	}
}
