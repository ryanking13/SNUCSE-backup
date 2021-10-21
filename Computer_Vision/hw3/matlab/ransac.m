function [line, inliers] = ransac(points, iter, thr, mininlier)
    points_size = size(points);
    previous_inlier_count = mininlier;
    
    for i = 1:iter
%         v1 = p(i);
%         v2 = p(i+1);
        p1 = fix(rand * points_size(2)) + 1;
        p2 = fix(rand * points_size(2)) + 1;
        v1 = points(:, p1);
        v2 = points(:, p2);
        
        inlier_count = 0;
        cur_inliers = zeros(points_size(2), 1);
        for j = 1:points_size(2)
            if j == p1 || j == p2
                continue;
            end
            
            p = points(:, j);
            dist = abs((v2(2) - v1(2)) * p(1) - (v2(1) - v1(1)) * p(2) + v2(1)*v1(2) - v2(2)*v1(1)) / sqrt((v2(2) - v1(2))^2 + (v2(1)-v1(1))^2);
            
            if dist < thr
                inlier_count = inlier_count + 1;
                cur_inliers(j) = 1;
            else
                cur_inliers(j) = 0;
            end
        end
        
        if inlier_count >= previous_inlier_count
            inliers = cur_inliers(:);
            previous_inlier_count = inlier_count;
            line = [v1 v2];
        end
        
        [sizeY, sizeX] = size(cur_inliers);
        color = zeros(sizeY, 3);
        color(:, 1) = cur_inliers(:);

        figure(i);
        scatter(points(1, :), points(2, :), 10, color);
        hold on;
        curline = [v1 v2];
        plot(curline(1, :), curline(2, :));
        hold off;
%         saveas(gcf, sprintf("ransac_%d.png", i));
    end
end
