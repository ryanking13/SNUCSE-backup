datadir     = '../data';    %the directory containing the images
resultsdir  = '../results'; %the directory for dumping results

%parameters
sigma1 = 1;
sigma2 = 2;

sigma = 1;
highThreshold = 0.7;
lowThreshold = 0.3;
%end of parameters

imglist = dir(sprintf('%s/*.jpg', datadir));

for i = 1:numel(imglist)
    
    %read in images%
    [path imgname dummy] = fileparts(imglist(i).name);
    img = imread(sprintf('%s/%s', datadir, imglist(i).name));
    
    if (ndims(img) == 3)
        img = rgb2gray(img);
    end
    
    img = double(img) / 255;
    
    % Laplacian
    Il = myLaplacianFilter(img);
    
    % Differential of Gaussian
    Ig1 = myGaussianFilter(img, sigma1);
    Ig2 = myGaussianFilter(img, sigma2);
    Idg = Ig2 - Ig1;
    
    % Canny Edge Detector
    % 1. Smoothing
    Ig = myGaussianFilter(img, sigma);
    % 2. Find gradient magnitude and direction
    [Im, Io] = mySobelFilter(Ig);
    % 3. Run non-maximum suppression
    In = myNonMaximumSuppression(Im, Io);
    % 4. Double thresholding
    Iedge = myDoubleThresholding(In, highThreshold, lowThreshold);
    
    %everything below here just saves the outputs to files%
    fname = sprintf('%s/%s_01Laplacian.pgm', resultsdir, imgname);
    imwrite((Il - min(Il(:))) ./ (max(Il(:)) - min(Il(:))), fname);
    fname = sprintf('%s/%s_02DoG.pgm', resultsdir, imgname);
    imwrite((Idg - min(Idg(:))) ./ (max(Idg(:)) - min(Idg(:))), fname);
    fname = sprintf('%s/%s_03Canny.pgm', resultsdir, imgname);
    imwrite(Iedge, fname);

end
    