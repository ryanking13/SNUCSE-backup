function [globalImg, directImg] = separateGlobalDirect(dirname)
    filepath = fullfile(dirname, '*.png');
    files = dir(filepath);
    numFiles = size(files);
    
    for i = 1:numFiles
        file = files(i).name;
        img = double(imread(sprintf('%s/%s', dirname, file)))/255;
        [imgY, imgX, imgZ] = size(img);
        
        if i == 1
            minImg = ones(imgY, imgX, imgZ);
            maxImg = zeros(imgY, imgX, imgZ);
        end
        
        maxImg = max(maxImg, img);
        minImg = min(minImg, img);
    end
    
    globalImg = maxImg - minImg;
    directImg = minImg * 2;
end
