 /**
 * Copyright (c) <2011>, <NetEase Corporation>
 * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
  *
  *    1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
  *    2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
  *    3. Neither the name of the <ORGANIZATION> nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package com.netease.webbench.blogbench.kv.redis;
 
 import com.netease.webbench.blogbench.BlogbenchPlugin;
 import com.netease.webbench.blogbench.dao.BlogDaoFactory;
 import com.netease.webbench.blogbench.dao.DataLoader;
 import com.netease.webbench.blogbench.misc.BbTestOptions;
 import com.netease.webbench.blogbench.misc.ParameterGenerator;
 import com.netease.webbench.common.DbOptions;
 
 /**
  * blogbench plugin for redis
  * 
  * @author LI WEIZHAO
  */
 public class BlogbenchRedisPlugin implements BlogbenchPlugin {
 	public BlogDaoFactory daoFacory = new RedisBlogDaoFactory();
 	
 	public BlogbenchRedisPlugin() {
 	}
 	
 	@Override
 	public DataLoader getDataLoader(DbOptions dbOpt, BbTestOptions bbTestOpt, 
 			ParameterGenerator parGen) throws Exception {
 		return new RedisDataLoader(dbOpt, bbTestOpt, parGen, daoFacory);
 	}
 
 	@Override
 	public BlogDaoFactory getBlogDaoFacory() throws Exception {
 		return daoFacory;
 	}
 
 	@Override
 	public void validateOptions(DbOptions dbOpt, BbTestOptions bbTestOpt)
 			throws IllegalArgumentException {
 		if (!"redis".equalsIgnoreCase(dbOpt.getDbType()))
 			throw new IllegalArgumentException("Wrong database type: " + dbOpt.getDbType() +
 					", should be 'redis' while you use redis plugin!");
 	}
 }
