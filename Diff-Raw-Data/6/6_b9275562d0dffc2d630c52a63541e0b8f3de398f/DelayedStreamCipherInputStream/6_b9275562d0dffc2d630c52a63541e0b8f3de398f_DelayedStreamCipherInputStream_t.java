 /*
 	This file is part of cellngine.
 
 	cellngine is free software: you can redistribute it and/or modify
 	it under the terms of the GNU Affero General Public License as published by
 	the Free Software Foundation, either version 3 of the License, or
 	(at your option) any later version.
 
 	cellngine is distributed in the hope that it will be useful,
 	but WITHOUT ANY WARRANTY; without even the implied warranty of
 	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 	GNU Affero General Public License for more details.
 
 	You should have received a copy of the GNU Affero General Public License
 	along with cellngine.  If not, see <http://www.gnu.org/licenses/>.
 */
 package com.cellngine.io;
 
 import java.io.IOException;
 import java.io.InputStream;
 
 import com.cellngine.crypto.StreamCipher;
 
 /**
 * This helper class automatically wraps a {@link com.cellngine.io.StreamCipherInputStream
 * StreamCipherInputStream} object around the provided {@link java.io.InputStream InputStream}
 * object when a given position
  * within the stream has been reached.
  *
  * @author A.J.A. Boer <jboer@jboer.nl>
  *
  */
 public class DelayedStreamCipherInputStream extends CommonInputStream
 {
 	private final int			encryptionOffset;
 	private final StreamCipher	streamCipher;
 	private boolean				positionReached	= false;
 
 	/**
 	 * @param inputStream
 	 *            The {@link java.io.InputStream InputStream} to wrap around.
 	 * @param streamCipher
 	 *            The {@link com.cellngine.crypto.StreamCipher StreamCipher} object to use for the
 	 *            encryption.
 	 * @param encryptionOffset
 	 *            An @{link java.lang.Integer Integer} specifying the offset within the
 	 *            {@link java.io.InputStream InputStream} where the encrypted data begins.
 	 */
 	public DelayedStreamCipherInputStream(final InputStream inputStream, final StreamCipher streamCipher,
 			final int encryptionOffset)
 	{
 		super(inputStream);
 		this.streamCipher = streamCipher;
 		this.encryptionOffset = encryptionOffset;
 	}
 
 	private void checkPosition()
 	{
 		if (!this.positionReached && this.position >= this.encryptionOffset)
 		{
 			this.positionReached = true;
 			this.in = new StreamCipherInputStream(this.in, this.streamCipher);
 		}
 	}
 
 	@Override
 	public int read() throws IOException
 	{
 		final int i = super.read();
 
 		this.checkPosition();
 
 		return i;
 	}
 
 	@Override
 	public int read(final byte[] b) throws IOException
 	{
 		return this.read(b, 0, b.length);
 	}
 
 	@Override
 	public int read(final byte[] b, final int off, final int len) throws IOException
 	{
 		if (this.position < this.encryptionOffset && this.position + len >= this.encryptionOffset)
 		{
 			int i, j;
 
 			final int encryptedBytesToRead = (int) (len - (this.encryptionOffset - this.position));
 			final int unencryptedBytesToRead = (len - encryptedBytesToRead);
 
 			i = super.read(b, off, unencryptedBytesToRead);
 
 			this.checkPosition();
 
 			j = super.read(b, off + unencryptedBytesToRead, encryptedBytesToRead);
 
 			if (j > -1)
 			{
 				i = i + j;
 			}
 
 			return i;
 		}
 		else
 		{
 			return super.read(b, off, len);
 		}
 	}
 
 	@Override
 	public long skip(final long n) throws IOException
 	{
 		if (this.position < this.encryptionOffset && this.position + n >= this.encryptionOffset)
 		{
 			final int encryptedBytesToSkip = (int) (n - (this.encryptionOffset - this.position));
 			final int unencryptedBytesToSkip = (int) (n - encryptedBytesToSkip);
 
 			/*
 			 * We have to make use of our own "forceSkip" implementation because "The skip method may,
 			 * for a variety of reasons, end up skipping over some smaller number of bytes, possibly 0."
 			 */
 
 			super.forceSkip(unencryptedBytesToSkip);
 
 			this.checkPosition();
 
 			super.forceSkip(encryptedBytesToSkip);
 
 			return n;
 		}
 		else
 		{
 			return super.skip(n);
 		}
 	}
 }
