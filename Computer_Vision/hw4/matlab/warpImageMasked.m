function warpim = warpImageMasked(im,H,W)

% Function to warp an image with a linear transform (affine or projective)
%
% ============================ Inputs ============================
% im - input image
%   A greyscale or color image (2D or 3D matrix), should be double not integer.
% H - transformation matrix
%   A 3x3 matrix for the transform to apply to the image
%   A pixel with coordinates [u v 1]' in the input image will move to
%   H[u v 1]' in the output image
% ============================ Outputs ============================
% warpim - output image
%   A greyscale or color image of the same size as im.
%   contains the transformed version of the input.

% get size of matrix
[R,C,D] = size(im);
warpim = zeros([R,C,D]);

% compute all points in the warped image
% [wX,wY] = meshgrid(1:C,1:R);
% allwp = [wX(:)';wY(:)';ones(1,C*R)];
wflat = find(W);
[wY, wX] = find(W);

wXflat = wX(:)';
wYflat = wY(:)';
[wR, wC] = size(wXflat);
allwp = [wXflat; wYflat; ones(1, wC);];

% compute the inverse warped positions in the original image
alluwp = H \ allwp;
alluwp(1,:) = alluwp(1,:) ./ alluwp(3,:);
alluwp(2,:) = alluwp(2,:) ./ alluwp(3,:);

% compute the image values for each dimension
for i = 1:D
    warpimi = interp2(im(:,:,i),alluwp(1,:),alluwp(2,:),'nearest',-1e6);
%     warpim(:,:,i) = reshape(warpimi,R,C);
    warpim(wflat + (i-1) * R * C) = warpimi;
end
