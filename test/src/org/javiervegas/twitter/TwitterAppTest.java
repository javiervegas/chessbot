/**
 * 
 */
package org.javiervegas.twitter;


import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import twitter4j.Twitter;
/**
 * @author javier
 *
 */
public class TwitterAppTest {

  private Twitter mockTwitter = mock(Twitter.class);

  @BeforeClass
  public static void setUp() throws Exception {
    
  }
  
  @Test
  public void meTest() {
    assertTrue(1>0);
  }

}
