package org.ng12306.tpms.runtime;

import java.util.Hashtable;
import java.util.UUID;

import org.joda.time.LocalDate;
import org.ng12306.tpms.ServiceBase;
import org.ng12306.tpms.TicketQueryEvent;

public class TestTicketPoolManager extends ServiceBase implements
		ITicketPoolManager {

	@Override
	public void initializeService() throws Exception {

		IRailwayRepository repo = this.getSite().getRequiredService(
				IRailwayRepository.class);

		LocalDate tom = new LocalDate().plusDays(1);
		LocalDate dat = tom.plusDays(1);

		int flag = 1;

		for (TrainNumber tn : repo.getTrainNumbers()) {
			for (int i = 0; i < 2; i++) {
				LocalDate date = flag > 0 ? tom : dat;
				flag *= -1;

				Train train = new Train();
				train.id = UUID.randomUUID();
				train.departureDate = date;
				train.trainNumber = tn;

				TestTicketPool pool = new TestTicketPool(train);
				pool.setSite(this.getSite());
				pool.initialize();

				String key = tn.name + "|" + date.toString();

				this._pools.put(key, pool);
			}

		}

		super.initializeService();
	};

	private Hashtable<String, ITicketPool> _pools = new Hashtable<String, ITicketPool>();

	public ITicketPool getPool(TicketQueryEvent args) {
		String key = args.trainNumber + "|" + args.date.toString();
		return this._pools.get(key);
	}

	@Override
	public void addPool(ITicketPool pool) {
		throw new UnsupportedOperationException();

	}

	@Override
	public void removePool(ITicketPool pool) {
		throw new UnsupportedOperationException();

	}

	@Override
	public ITicketPool[] getAlivePools() {
		return this._pools.values().toArray(new ITicketPool[0]);
	}

}
