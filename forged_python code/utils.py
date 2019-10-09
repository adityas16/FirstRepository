import scipy.stats


class random_variable:
    dist = dict()
    def __init__(self, dist):
        #TODO: Normalize the distribution to make sure it adds to 1
        self.dist = dist


    def subtract_from(self,k):
        ''' Return k-x'''
        #TODO: Can be optimized to ust he same dict as X with a shift of k
        new_dist = dict()
        for (x, p) in self.dist.items():
            new_dist[k-x] = p
        return random_variable(new_dist)

    def expect(self,f):
        val = 0
        for (x,p) in self.dist.items():
            val = val + f(x) * p
        return val
