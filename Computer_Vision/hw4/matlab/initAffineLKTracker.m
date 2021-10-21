function [affineLKContext] = initAffineLKTracker(img, msk)

%     load('../data/initTest.mat', 'affineLKContext', 'img');

    [imgR, imgC] = size(img);
    template = img .* msk; % template image (T)
%     template = img;
    jacobian = zeros(imgR * imgC, 6); % jacobian matrix (pixel * 6)
    
    [gradX, gradY] = imgradientxy(template, 'central'); % gradient (J = G * p)
%     [gradX, gradY] = gradient(double(template)); % gradient (J = G * p)
    
    gradXflat = gradX(:);
    gradYflat = gradY(:);
    
    for c = 1:imgC
        for r = 1:imgR
            idx = (c-1) * imgR + r;
            jacobian(idx, :) = [gradXflat(idx), gradYflat(idx)] * [c, 0, r, 0, 1, 0; 0, c, 0, r, 0, 1];
        end
    end
    
    hessianInv = inv(jacobian' * jacobian);
    affineLKContext = struct('Jacobian', jacobian, 'HessianInv', hessianInv, 'Exception', false);
    
%     disp(affineLKContext2.Jacobian - affineLKContext.Jacobian);
end
