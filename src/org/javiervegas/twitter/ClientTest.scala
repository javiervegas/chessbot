/**
 * 
 */
package org.javiervegas.twitter

import twitter4j.{Twitter,Paging,Status}

import java.lang.reflect.Field
import java.util.ArrayList
import java.util.List


//import org.specs.Specification
//import org.specs.mock.Mockito
import org.mockito.Mock
import org.mockito.Matchers.anyObject
import org.mockito.Matchers.argThat
import org.mockito.Matchers.eq
import org.mockito.Mockito.mock
import org.mockito.Mockito.when
/*
import org.junit.Assert._
import org.junit.After
import org.junit.AfterClass
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
*/

object ClientTest extends Application { 
  /* with Mockito with Specification{
    val twitterField = Client.getClass.getDeclaredField("mytwitter")
//    val mctwitter = mock[Twitter]
    twitterField.setAccessible(true)
//    twitterField.set(null,mctwitter)
//    when(mctwitter.getMentions(new Paging(1L))).thenReturn(new ArrayList[Status]())
*/
    val result = ChessClient.get.fetch(1L);
    //if (r
    //assertTrue(result._2.size == 0)
    println(result._1)
    println(result._2)
    println("done")
}
