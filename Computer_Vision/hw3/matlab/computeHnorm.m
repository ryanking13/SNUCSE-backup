function H = computeHnorm(p1, p2)
    [r, c] = size(p1);
    
    % homogeneous coord
    hp1 = zeros(r + 1, c);
    hp1(1:r, :) = p1;
    hp1(r + 1, :) = ones(1, c);
    hp2 = zeros(r + 1, c);
    hp2(1:r, :) = p2;
    hp2(r + 1, :) = ones(1, c);

    n1 = computeNormalizationMatrix(p1);
    n2 = computeNormalizationMatrix(p2);
%     np1 = n2(:, 1:2) * p1;
%     np2 = n1(:, 1:2) * p2;

    % normalized coord
    np1 = n2 * hp1;
    np2 = n1 * hp2;
    
    H = computeH(np1, np2);
    % resize H
    H = (n2 \ H) * n1;
end

function n = computeNormalizationMatrix(p)
    [r, c] = size(p);
    ravg = sum(p, 2) / c;
    
    n = [1 0 -ravg(1);
         0 1 -ravg(2);
         0 0 1;];

    diff = p - ravg;
    dist = diff .^ 2;
    distsum = sum(sum(dist));
    scale = (distsum / (sqrt(2) * c)) ^ (-1);
    n = [scale 0 0;
         0 scale 0;
         0 0 1;] * n;
end
