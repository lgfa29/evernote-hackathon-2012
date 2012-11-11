﻿// Copyright (c) 2011, Chute Corporation. All rights reserved.
// 
//  Redistribution and use in source and binary forms, with or without modification, 
//  are permitted provided that the following conditions are met:
// 
//     * Redistributions of source code must retain the above copyright notice, this 
//       list of conditions and the following disclaimer.
//     * Redistributions in binary form must reproduce the above copyright notice,
//       this list of conditions and the following disclaimer in the documentation
//       and/or other materials provided with the distribution.
//     * Neither the name of the  Chute Corporation nor the names
//       of its contributors may be used to endorse or promote products derived from
//       this software without specific prior written permission.
// 
//  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
//  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED 
//  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
//  IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
//  INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
//  BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
//  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
//  LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
//  OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
//  OF THE POSSIBILITY OF SUCH DAMAGE.
// 
package com.chute.sdk.api.asset;

import android.graphics.Bitmap;

import com.chute.sdk.model.GCAssetModel;

/**
 * <b> IMPORTANT!! runs the updates in the thread that executed the request</b>
 * 
 * @author DArkO
 * 
 */
public interface GCUploadProgressListener {
    /**
     * This is triggered when the
     * 
     * @param id
     *            the id of the asset you are currently uploading
     * @param filepath
     *            the filepath of the asset
     * @param thumbnail
     *            a small thumbnail that will be created from the asset before
     *            the upload starts
     */
    public void onUploadStarted(GCAssetModel asset, final Bitmap thumbnail);

    /**
     * @param total
     *            the total size of the asset
     * @param current
     *            the ammount of data uploaded
     */
    public void onProgress(long total, long current);

    /**
     * This triggers when the upload has finished, it doesnt carry the
     * information about the status of the upload request
     * 
     * @param id
     *            the id of the asset
     * @param filepath
     *            the filepath of the asset
     */
    public void onUploadFinished(GCAssetModel assetModel);
}
