function [cost, grad, pred_prob] = mlp_cost(weight, ei, X, y, prediction_on)

    po = false;
    if exist('prediction_on','var')
      po = prediction_on;
    end


    %% forward propagation 

    % reshape weight(:) into network (weight stack)
    wStack = params2stack(weight, ei);
    L = numel(ei.layer_sizes);
    numHidden = numel(ei.layer_sizes) - 1;
    
    activation = cell(L, 1);

    %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
    %%%
    %%%             TODO: YOUR CODE HERE [10 points]                           
    %%%
    %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
    %%% OBJECTIVE:implement forward protagation, using currently learned network   
    %%% weight 'wStack', and activation function "sigmoid()". 
    %%% Your code should support an arbituary number of hidden layers 
    %%% 'numHidden', so that you can change # of hidden layers later.            
    %%%
    %%% OUTPUT: the outcome of last layer of our netowork is supposed to 
    %%% be the probability vector 'pred_prob' resulting from softmax. 
    %%% 
    %%% Once you get 'pred_prob', calculate cross-entropy cost and save
    %%% it into 'ceCost'
    %%%
    %====================================================================%

    % forward propagation [[[here]]]
    newX = X;
    for l = 1 : L
%        activation{l}=zeros(ei.layer_sizes(l),size(X,2));
        activation{l} = wStack{l}.W * newX + wStack{l}.b;
        if l == L
            activation{l} = softmax(activation{l}, 1);
        else
            activation{l} = sigmoid(activation{l});
        end
        newX = activation{l};
    end
    pred_prob = activation{L};
 
    % return when predictions flage "po" is true.
    if po
      cost = -1; ceCost = -1; wCost = -1; grad = [];  
      return;
    end
    
    % cross-entropy cost [[[here]]]
%     ceCost = 0;
    yOneHot = zeros( size( y, 1 ), 10);
    for i = 1:10
        rows = y == i;
        yOneHot( rows, i ) = 1;
    end
    
    ceCost = sum(yOneHot' .* log(pred_prob));
    ceCost = -sum(ceCost);
    
    %====================================================================%
    %% Compute gradient of your Network (backpropagation) 

    % Backpropagation of Gradient
    gStack = cell(L,1); % cell(wStack);
    
    %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
    %%%
    %%% OBJECTIVE:calculate gradient of newtork using backrotagation technique.
    %%%  
    %%% HOW: Since we are going to calculate the gradient of each weight 
    %%% (W and b) in our network (wStack), we basically need similar data 
    %%% structure to save the gradients for them. 
    %%% 
    %%% We defined 'gStack' similar to 'wStatck' to save these gradients
    %%% 
    %%% First, calculate the gradient of weights of last layer from the 
    %%% cross entropy cost, and save them into 'gStack{L}.W' and 
    %%% 'gStack{L}.b' where L stands for last layer's index.
    %%%      
    %%% Second, calculate the gradient of weight for each hidden layer 'l' 
    %%% and save them into 'gStack{l}.W' and 'gStack{l}.b'. And so on.. 
    %%% until you reach the very first hidden layer. 
    %%%
    %%% Later, 'gStack' will be vectorized and stored in 'grad'. 
    %%%
    %====================================================================%
    
    % Gradient of activation unit
    switch ei.activation_fun
        case 'sigmod'
            gradFunc = @(x) x .* (1-x); 
    end

    idx_y = zeros(size(pred_prob));
    idx = sub2ind(size(idx_y), y, 1:size(pred_prob,2));
    idx_y(idx) = 1;
    diff = pred_prob - idx_y;

    for l = L : -1 : 1
        if l == L
            delta = diff;
            gStack{l}.W = delta * activation{l-1}'; % gradient of softmax unit with cross-entropy cost is simple
        elseif l > 1
            delta = wStack{l+1}.W' * delta .* gradFunc(activation{l});     
            gStack{l}.W = delta * activation{l-1}';
        else % l ==1 
            delta = wStack{l+1}.W' * delta .* gradFunc(activation{l});     
            gStack{l}.W = delta * X'; % X == activation{0}
        end

        gStack{l}.b = sum(delta, 2);
    end

    %====================================================================%
    
    %% computing weight regularization cost and gradient
    wCost = 0;
    for l = 1:numHidden+1
        wCost = wCost + .5 * ei.lambda * sum(wStack{l}.W(:) .^ 2);
    end

    % Computing the gradient of the regularization term.
    for l = numHidden : -1 : 1
        gStack{l}.W = gStack{l}.W + ei.lambda * wStack{l}.W;
    end

    
    %% reshape gradients stack into vector form and return
    grad = stack2weight(gStack);
    cost = ceCost + wCost;
    
end
