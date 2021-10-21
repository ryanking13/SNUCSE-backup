function Wout = affineTrackerMasked(img, tmp, mask, W, context, nIter)
    jacobian = context.Jacobian;
    hessianInv = context.HessianInv;
    
    newW = W;
    tmpMasked = tmp .* mask;
    tmpMaskedFlat = tmpMasked(:);
    
    for i=1:nIter
        imgWarped = warpImageMasked(img, newW, mask);
%         imgWarped = warpImage(img, newW);
        deltaP = hessianInv * jacobian' * (tmpMaskedFlat - imgWarped(:));     
        pW = [1 + deltaP(1), deltaP(3), deltaP(5);
              deltaP(2), 1 + deltaP(4), deltaP(6);
              0, 0, 1];
        newW = newW / pW;
    end
    
    Wout = newW;
end
