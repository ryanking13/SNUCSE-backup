function H = computeH(p1, p2)
    [r, c] = size(p2);
    A = zeros(c * 2, 9);
    
    
    for i = 1:c
        A(i*2 - 1, :) = [p2(1, i), p2(2, i), 1, 0, 0, 0, -p1(1, i) * p2(1, i), -p1(1, i) * p2(2, i), -p1(1, i)];
        A(i*2, :) = [0, 0, 0, p2(1, i), p2(2, i), 1, -p1(2, i) * p2(1, i), -p1(2, i) * p2(2, i), -p1(2, i)];
    end
    
    [V, D] = eig(transpose(A) * A);
%     disp(D);
    H = V(:, 1);
    H = reshape(H, 3, 3);
    H = transpose(H);
    
end
